//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/detectability.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'observability_coverage.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ObservabilityCoverage {
  /// Returns a new [ObservabilityCoverage] instance.
  ObservabilityCoverage({

    required  this.family,

    required  this.at,

     this.band,

    required  this.absenceMinDetect,

    required  this.items,

     this.nextCursor,
  });

  @JsonKey(
    
    name: r'family',
    required: true,
    includeIfNull: false,
  )


  final String family;



  @JsonKey(
    
    name: r'at',
    required: true,
    includeIfNull: false,
  )


  final DateTime at;



  @JsonKey(
    
    name: r'band',
    required: false,
    includeIfNull: false,
  )


  final String? band;



          // minimum: 0
          // maximum: 1
  @JsonKey(
    
    name: r'absenceMinDetect',
    required: true,
    includeIfNull: false,
  )


  final num absenceMinDetect;



  @JsonKey(
    
    name: r'items',
    required: true,
    includeIfNull: false,
  )


  final List<Detectability> items;



  @JsonKey(
    
    name: r'nextCursor',
    required: false,
    includeIfNull: false,
  )


  final String? nextCursor;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ObservabilityCoverage &&
      other.family == family &&
      other.at == at &&
      other.band == band &&
      other.absenceMinDetect == absenceMinDetect &&
      other.items == items &&
      other.nextCursor == nextCursor;

    @override
    int get hashCode =>
        family.hashCode +
        at.hashCode +
        (band == null ? 0 : band.hashCode) +
        absenceMinDetect.hashCode +
        items.hashCode +
        (nextCursor == null ? 0 : nextCursor.hashCode);

  factory ObservabilityCoverage.fromJson(Map<String, dynamic> json) => _$ObservabilityCoverageFromJson(json);

  Map<String, dynamic> toJson() => _$ObservabilityCoverageToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

