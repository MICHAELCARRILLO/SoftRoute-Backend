package com.softroute.softroutebackend.softroute.shipment.service;

import com.softroute.softroutebackend.shared.exception.ResourceNotFoundException;
import com.softroute.softroutebackend.shared.exception.ResourceValidationException;
import com.softroute.softroutebackend.softroute.shipment.domain.model.Shipment;
import com.softroute.softroutebackend.softroute.shipment.domain.persistence.ShipmentRepository;
import com.softroute.softroutebackend.softroute.shipment.domain.service.ShipmentService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class ShipmentServiceImp implements ShipmentService {
    private static final String ENTITY = "Shipment";

    private final ShipmentRepository shipmentRepository;

    private final Validator validator;

    public ShipmentServiceImp(ShipmentRepository shipmentRepository,Validator validator) {
        this.shipmentRepository=shipmentRepository;
        this.validator=validator;
    }
    @Override
    public List<Shipment> getAll() {
        return shipmentRepository.findAll();
    }

    @Override
    public Shipment getId(Long shipment_id) {
        return shipmentRepository.findById(shipment_id).orElseThrow(()->new ResourceNotFoundException(ENTITY,shipment_id));
    }

    @Override
    public Shipment getByCode(Long code) {
        return shipmentRepository.findByCode(code);
    }

    @Override
    public List<Shipment> getByFreight(Double freight) {
        return shipmentRepository.findByFreight(freight);
    }

    @Override
    public List<Shipment> getByQuantity(Integer quantity) {
        return shipmentRepository.findByQuantity(quantity);
    }

    @Override
    public List<Shipment> getByDeliveredDate(Date deliveryDate) {
        return shipmentRepository.findByDeliveredDate(deliveryDate);
    }

    @Override
    public List<Shipment> getByArrivalDate(Date arrivalDate) {
        return shipmentRepository.findByArrivalDate(arrivalDate);
    }

    @Override
    public Shipment create(Shipment shipment) {
        Set<ConstraintViolation<Shipment>> violations = validator.validate(shipment);

        if(!violations.isEmpty())
            throw new ResourceValidationException(ENTITY, violations);

        Shipment shipmentWithId = shipmentRepository.findShipmentById(shipment.getId());

        if (shipmentWithId!=null)
            throw new ResourceValidationException(ENTITY,
                    "A shipment with the same id already exists.");

        Shipment shipmentWithCode=shipmentRepository.findByCode(shipment.getCode());
        if(shipmentWithCode!=null)
            throw new ResourceValidationException(ENTITY, "A shipment with the same code already exists");

        return shipmentRepository.save(shipment);
    }

    @Override
    public Shipment update(Long shipment_id, Shipment request) {
        Set<ConstraintViolation<Shipment>> violations = validator.validate(request);

        if (!violations.isEmpty())
            throw new ResourceValidationException(ENTITY, violations);

        Shipment shipmentWithCode=shipmentRepository.findByCode(request.getCode());
        if(shipmentWithCode!=null)
            throw new ResourceValidationException(ENTITY, "A shipment with the same code already exists");

        return shipmentRepository.findById(shipment_id).map(shipment ->
                        shipmentRepository.save(shipment.withDescription(request.getDescription())
                                .withFreight(request.getFreight())
                                .withConsignee(request.getConsignee())
                                .withQuantity(request.getQuantity())
                                .withArrivalDate(request.getArrivalDate())
                                .withDeliveredDate(request.getDeliveredDate())))
                .orElseThrow(() -> new ResourceNotFoundException(ENTITY, shipment_id));
    }

    @Override
    public ResponseEntity<?> delete(Long shipment_id) {
        return shipmentRepository.findById(shipment_id).map(
                agency -> {
                    shipmentRepository.delete(agency);
                    return ResponseEntity.ok().build();
                }).orElseThrow(() -> new ResourceNotFoundException(ENTITY, shipment_id));
    }
}