//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/impact_rate_interval.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'impact_rate.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ImpactRate {
  /// Returns a new [ImpactRate] instance.
  ImpactRate({

    required  this.hazardClass,

    required  this.impactFamily,

    required  this.regionClass,

     this.lagHours,

     this.rate,

     this.interval,

    required  this.n,

     this.nImpacts,

     this.source_,

    required  this.status,

     this.baseRateId,

    required  this.computedAt,
  });

  @JsonKey(
    
    name: r'hazardClass',
    required: true,
    includeIfNull: false,
  )


  final String hazardClass;



  @JsonKey(
    
    name: r'impactFamily',
    required: true,
    includeIfNull: false,
  )


  final String impactFamily;



  @JsonKey(
    
    name: r'regionClass',
    required: true,
    includeIfNull: false,
  )


  final String regionClass;



  @JsonKey(
    
    name: r'lagHours',
    required: false,
    includeIfNull: false,
  )


  final int? lagHours;



          // minimum: 0
          // maximum: 1
  @JsonKey(
    
    name: r'rate',
    required: false,
    includeIfNull: false,
  )


  final num? rate;



  @JsonKey(
    
    name: r'interval',
    required: false,
    includeIfNull: false,
  )


  final ImpactRateInterval? interval;



      /// Hazard occurrences
  @JsonKey(
    
    name: r'n',
    required: true,
    includeIfNull: false,
  )


  final int n;



  @JsonKey(
    
    name: r'nImpacts',
    required: false,
    includeIfNull: false,
  )


  final int? nImpacts;



  @JsonKey(
    
    name: r'source',
    required: false,
    includeIfNull: false,
  )


  final ImpactRateSource_Enum? source_;



      /// `insufficient` when n < 20 (pair skipped)
  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final ImpactRateStatusEnum status;



  @JsonKey(
    
    name: r'baseRateId',
    required: false,
    includeIfNull: false,
  )


  final String? baseRateId;



  @JsonKey(
    
    name: r'computedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime computedAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ImpactRate &&
      other.hazardClass == hazardClass &&
      other.impactFamily == impactFamily &&
      other.regionClass == regionClass &&
      other.lagHours == lagHours &&
      other.rate == rate &&
      other.interval == interval &&
      other.n == n &&
      other.nImpacts == nImpacts &&
      other.source_ == source_ &&
      other.status == status &&
      other.baseRateId == baseRateId &&
      other.computedAt == computedAt;

    @override
    int get hashCode =>
        hazardClass.hashCode +
        impactFamily.hashCode +
        regionClass.hashCode +
        lagHours.hashCode +
        (rate == null ? 0 : rate.hashCode) +
        (interval == null ? 0 : interval.hashCode) +
        n.hashCode +
        (nImpacts == null ? 0 : nImpacts.hashCode) +
        source_.hashCode +
        status.hashCode +
        (baseRateId == null ? 0 : baseRateId.hashCode) +
        computedAt.hashCode;

  factory ImpactRate.fromJson(Map<String, dynamic> json) => _$ImpactRateFromJson(json);

  Map<String, dynamic> toJson() => _$ImpactRateToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ImpactRateSource_Enum {
@JsonValue(r'own-history')
ownHistory(r'own-history'),
@JsonValue(r'reference-import')
referenceImport(r'reference-import'),
@JsonValue(r'mixed')
mixed(r'mixed');

const ImpactRateSource_Enum(this.value);

final String value;

@override
String toString() => value;
}


/// `insufficient` when n < 20 (pair skipped)
enum ImpactRateStatusEnum {
@JsonValue(r'ok')
ok(r'ok'),
@JsonValue(r'insufficient')
insufficient(r'insufficient');

const ImpactRateStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


