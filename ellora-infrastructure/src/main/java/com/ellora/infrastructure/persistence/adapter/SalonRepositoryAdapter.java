package com.ellora.infrastructure.persistence.adapter;

import com.ellora.domain.constant.SalonStatus;
import com.ellora.domain.model.Salon;
import com.ellora.domain.repository.SalonRepository;
import com.ellora.infrastructure.persistence.entity.SalonEntity;
import com.ellora.infrastructure.persistence.mapper.SalonMapper;
import com.ellora.infrastructure.persistence.repository.SpringDataSalonRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SalonRepositoryAdapter implements SalonRepository {

    private final SpringDataSalonRepository springDataSalonRepository;
    private final SalonMapper salonMapper;
    private final EntityManager entityManager;

    @Override
    public Optional<Salon> findById(Long id) {
        return springDataSalonRepository.findById(id).map(salonMapper::toDomain);
    }

    @Override
    public List<Salon> findByStatus(SalonStatus status, int offset, int limit) {
        int pageNumber = offset / Math.max(1, limit);
        Page<SalonEntity> page = springDataSalonRepository.findByStatus(
                status.name(),
                PageRequest.of(pageNumber, limit, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "id"))
        );
        return page.getContent().stream().map(salonMapper::toDomain).toList();
    }

    @Override
    public List<Salon> searchActiveSalons(String city, String keyword, int offset, int limit) {
        return searchActiveSalons(city, keyword, null, null, null, "newest", offset, limit);
    }

    @Override
    public List<Salon> searchActiveSalons(String city, String keyword, Double latitude, Double longitude, Double radiusKm, String sort, int offset, int limit) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = cb.createTupleQuery();
        Root<SalonEntity> salon = query.from(SalonEntity.class);

        Expression<Double> distanceExpr = distanceExpression(cb, salon, latitude, longitude);
        List<Predicate> predicates = buildPredicates(cb, salon, city, keyword, latitude, longitude, radiusKm, sort, distanceExpr);

        query.multiselect(salon, distanceExpr);
        query.where(predicates.toArray(Predicate[]::new));

        List<Order> orders = new ArrayList<>();
        if ("distance".equalsIgnoreCase(sort) && latitude != null && longitude != null) {
            orders.add(cb.asc(distanceExpr));
            orders.add(cb.desc(salon.get("id")));
        } else if ("rating".equalsIgnoreCase(sort)) {
            orders.add(cb.desc(cb.coalesce(salon.get("averageRating"), 0)));
            orders.add(cb.desc(cb.coalesce(salon.get("totalReviews"), 0)));
            orders.add(cb.desc(salon.get("id")));
        } else {
            orders.add(cb.desc(salon.get("id")));
        }
        query.orderBy(orders);

        List<Tuple> tuples = entityManager.createQuery(query)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();

        List<Salon> result = new ArrayList<>();
        for (Tuple tuple : tuples) {
            SalonEntity entity = tuple.get(0, SalonEntity.class);
            Double dist = tuple.get(1, Double.class);
            Salon domain = salonMapper.toDomain(entity);
            if (dist != null && !Double.isNaN(dist) && !Double.isInfinite(dist)) {
                domain.setDistanceKm(Math.round(dist * 10.0) / 10.0);
            }
            result.add(domain);
        }
        return result;
    }

    @Override
    public long countActiveSalons(String city, String keyword) {
        return countActiveSalons(city, keyword, null, null, null);
    }

    @Override
    public long countActiveSalons(String city, String keyword, Double latitude, Double longitude, Double radiusKm) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<SalonEntity> countSalon = countQuery.from(SalonEntity.class);

        Expression<Double> countDistExpr = distanceExpression(cb, countSalon, latitude, longitude);
        List<Predicate> countPredicates = buildPredicates(cb, countSalon, city, keyword, latitude, longitude, radiusKm, null, countDistExpr);

        countQuery.select(cb.count(countSalon));
        countQuery.where(countPredicates.toArray(Predicate[]::new));

        Long total = entityManager.createQuery(countQuery).getSingleResult();
        return total != null ? total : 0L;
    }

    private List<Predicate> buildPredicates(
            CriteriaBuilder cb, Root<SalonEntity> salon,
            String city, String keyword,
            Double latitude, Double longitude, Double radiusKm, String sort,
            Expression<Double> distanceExpr) {
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(salon.get("status"), "ACTIVE"));
        predicates.add(cb.or(cb.isNull(salon.get("isOverdue")), cb.isFalse(salon.get("isOverdue"))));

        if (city != null && !city.trim().isEmpty()) {
            String cityLower = "%" + city.trim().toLowerCase(Locale.ROOT) + "%";
            predicates.add(cb.or(
                    cb.like(cb.lower(salon.get("city")), cityLower),
                    cb.like(cb.lower(salon.get("district")), cityLower),
                    cb.like(cb.lower(salon.get("address")), cityLower)
            ));
        }

        if (keyword != null && !keyword.trim().isEmpty()) {
            String kwLower = "%" + keyword.trim().toLowerCase(Locale.ROOT) + "%";
            predicates.add(cb.or(
                    cb.like(cb.lower(salon.get("name")), kwLower),
                    cb.like(cb.lower(salon.get("address")), kwLower),
                    cb.like(cb.lower(salon.get("city")), kwLower),
                    cb.like(cb.lower(salon.get("district")), kwLower)
            ));
        }

        if (latitude != null && longitude != null && radiusKm != null && radiusKm > 0) {
            predicates.add(validPin(cb, salon));
            predicates.add(cb.le(distanceExpr, radiusKm));
        } else if ("distance".equalsIgnoreCase(sort)) {
            predicates.add(validPin(cb, salon));
        }

        return predicates;
    }

    private Predicate validPin(CriteriaBuilder cb, Root<SalonEntity> salon) {
        return cb.and(
                cb.isNotNull(salon.get("latitude")),
                cb.isNotNull(salon.get("longitude")),
                cb.between(salon.get("latitude"), -90.0, 90.0),
                cb.between(salon.get("longitude"), -180.0, 180.0)
        );
    }

    private Expression<Double> distanceExpression(CriteriaBuilder cb, Root<SalonEntity> salon, Double lat, Double lng) {
        if (lat == null || lng == null) {
            return cb.nullLiteral(Double.class);
        }
        Expression<Double> salonLatRad = cb.function("radians", Double.class, salon.get("latitude"));
        Expression<Double> salonLngRad = cb.function("radians", Double.class, salon.get("longitude"));
        double centerLatRad = Math.toRadians(lat);
        double centerLngRad = Math.toRadians(lng);

        Expression<Double> sinProduct = cb.prod(cb.function("sin", Double.class, salonLatRad), Math.sin(centerLatRad));
        Expression<Double> cosProduct = cb.prod(
                cb.prod(cb.function("cos", Double.class, salonLatRad), Math.cos(centerLatRad)),
                cb.function("cos", Double.class, cb.diff(salonLngRad, centerLngRad))
        );
        Expression<Double> dot = cb.sum(sinProduct, cosProduct);
        Expression<Double> clamped = cb.function("least", Double.class, cb.literal(1.0),
                cb.function("greatest", Double.class, cb.literal(-1.0), dot));

        return cb.<Double>selectCase()
                .when(validPin(cb, salon), cb.prod(cb.function("acos", Double.class, clamped), 6371.0088))
                .otherwise(cb.nullLiteral(Double.class));
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    @Override
    public List<Salon> findByOwnerId(UUID ownerId) {
        return springDataSalonRepository.findByOwnerIdOrderByIdDesc(ownerId).stream().map(salonMapper::toDomain).toList();
    }

    @Override
    public long countByStatus(SalonStatus status) {
        return springDataSalonRepository.countByStatus(status != null ? status.name() : null);
    }

    @Override
    public long count() {
        return springDataSalonRepository.count();
    }

    @Override
    public List<Salon> findAll() {
        return springDataSalonRepository.findAll().stream().map(salonMapper::toDomain).toList();
    }

    @Override
    public List<Salon> findAll(int offset, int limit) {
        int pageNumber = offset / Math.max(1, limit);
        Page<SalonEntity> page = springDataSalonRepository.findAll(
                PageRequest.of(pageNumber, limit, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "id"))
        );
        return page.getContent().stream().map(salonMapper::toDomain).toList();
    }

    @Override
    public Salon save(Salon salon) {
        SalonEntity entity = salonMapper.toEntity(salon);
        SalonEntity saved = springDataSalonRepository.save(entity);
        return salonMapper.toDomain(saved);
    }
}
