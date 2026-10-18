//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/chronicle_summary_layers_inner.dart';
import 'package:foresignal_api/src/model/object_envelope.dart';
import 'package:foresignal_api/src/model/chronicle_summary_storage.dart';
import 'package:foresignal_api/src/model/chronicle_summary_top_entities_inner.dart';
import 'package:foresignal_api/src/model/time_range.dart';
import 'package:foresignal_api/src/model/chronicle_summary_gaps_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'chronicle_summary.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ChronicleSummary {
  /// Returns a new [ChronicleSummary] instance.
  ChronicleSummary({

    required  this.chronicleId,

    required  this.span,

    required  this.layers,

     this.topEntities,

     this.situations,

     this.hypothesisSets,

     this.forecasts,

    required  this.storage,

    required  this.gaps,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'chronicleId',
    required: true,
    includeIfNull: false,
  )


  final String chronicleId;



  @JsonKey(
    
    name: r'span',
    required: true,
    includeIfNull: false,
  )


  final TimeRange span;



  @JsonKey(
    
    name: r'layers',
    required: true,
    includeIfNull: false,
  )


  final List<ChronicleSummaryLayersInner> layers;



  @JsonKey(
    
    name: r'topEntities',
    required: false,
    includeIfNull: false,
  )


  final List<ChronicleSummaryTopEntitiesInner>? topEntities;



  @JsonKey(
    
    name: r'situations',
    required: false,
    includeIfNull: false,
  )


  final List<ObjectEnvelope>? situations;



  @JsonKey(
    
    name: r'hypothesisSets',
    required: false,
    includeIfNull: false,
  )


  final List<ObjectEnvelope>? hypothesisSets;



  @JsonKey(
    
    name: r'forecasts',
    required: false,
    includeIfNull: false,
  )


  final List<ObjectEnvelope>? forecasts;



  @JsonKey(
    
    name: r'storage',
    required: true,
    includeIfNull: false,
  )


  final ChronicleSummaryStorage storage;



  @JsonKey(
    
    name: r'gaps',
    required: true,
    includeIfNull: false,
  )


  final List<ChronicleSummaryGapsInner> gaps;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ChronicleSummary &&
      other.chronicleId == chronicleId &&
      other.span == span &&
      other.layers == layers &&
      other.topEntities == topEntities &&
      other.situations == situations &&
      other.hypothesisSets == hypothesisSets &&
      other.forecasts == forecasts &&
      other.storage == storage &&
      other.gaps == gaps;

    @override
    int get hashCode =>
        chronicleId.hashCode +
        span.hashCode +
        layers.hashCode +
        topEntities.hashCode +
        situations.hashCode +
        hypothesisSets.hashCode +
        forecasts.hashCode +
        storage.hashCode +
        gaps.hashCode;

  factory ChronicleSummary.fromJson(Map<String, dynamic> json) => _$ChronicleSummaryFromJson(json);

  Map<String, dynamic> toJson() => _$ChronicleSummaryToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

