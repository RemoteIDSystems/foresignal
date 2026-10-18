//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/caveat.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'forecast_resolution.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ForecastResolution {
  /// Returns a new [ForecastResolution] instance.
  ForecastResolution({

     this.outcome,

     this.value,

    required  this.resolvedAt,

    required  this.resolvedBy,

     this.early,

     this.asOf,

    required  this.evidence,

     this.note,

     this.caveats,

     this.manifestId,
  });

      /// Binary `yes`/`no`, or the label of a multiple/ordered question
  @JsonKey(
    
    name: r'outcome',
    required: false,
    includeIfNull: false,
  )


  final String? outcome;



      /// Numeric questions
  @JsonKey(
    
    name: r'value',
    required: false,
    includeIfNull: false,
  )


  final num? value;



  @JsonKey(
    
    name: r'resolvedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime resolvedAt;



      /// Resolver id and version, or the principal for manual resolutions
  @JsonKey(
    
    name: r'resolvedBy',
    required: true,
    includeIfNull: false,
  )


  final String resolvedBy;



      /// Resolved before `resolve_by` because the event occurred
  @JsonKey(
    
    name: r'early',
    required: false,
    includeIfNull: false,
  )


  final bool? early;



      /// Knowledge cutoff used (`resolve_by` + release lag)
  @JsonKey(
    
    name: r'asOf',
    required: false,
    includeIfNull: false,
  )


  final DateTime? asOf;



      /// Pinned records or rollup rows
  @JsonKey(
    
    name: r'evidence',
    required: true,
    includeIfNull: false,
  )


  final List<String> evidence;



  @JsonKey(
    
    name: r'note',
    required: false,
    includeIfNull: false,
  )


  final String? note;



      /// e.g. late data still missing after 14 days
  @JsonKey(
    
    name: r'caveats',
    required: false,
    includeIfNull: false,
  )


  final List<Caveat>? caveats;



      /// ULID identifier
  @JsonKey(
    
    name: r'manifestId',
    required: false,
    includeIfNull: false,
  )


  final String? manifestId;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ForecastResolution &&
      other.outcome == outcome &&
      other.value == value &&
      other.resolvedAt == resolvedAt &&
      other.resolvedBy == resolvedBy &&
      other.early == early &&
      other.asOf == asOf &&
      other.evidence == evidence &&
      other.note == note &&
      other.caveats == caveats &&
      other.manifestId == manifestId;

    @override
    int get hashCode =>
        (outcome == null ? 0 : outcome.hashCode) +
        (value == null ? 0 : value.hashCode) +
        resolvedAt.hashCode +
        resolvedBy.hashCode +
        early.hashCode +
        (asOf == null ? 0 : asOf.hashCode) +
        evidence.hashCode +
        (note == null ? 0 : note.hashCode) +
        caveats.hashCode +
        (manifestId == null ? 0 : manifestId.hashCode);

  factory ForecastResolution.fromJson(Map<String, dynamic> json) => _$ForecastResolutionFromJson(json);

  Map<String, dynamic> toJson() => _$ForecastResolutionToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

