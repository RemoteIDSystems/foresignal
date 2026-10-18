//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/chronicle_layer_selector.dart';
import 'package:foresignal_api/src/model/chronicle_area.dart';
import 'package:foresignal_api/src/model/chronicle_layer_stats.dart';
import 'package:foresignal_api/src/model/chronicle_layer_estimate.dart';
import 'package:foresignal_api/src/model/capture_mode.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'chronicle_layer.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ChronicleLayer {
  /// Returns a new [ChronicleLayer] instance.
  ChronicleLayer({

    required  this.id,

    required  this.chronicleId,

    required  this.name,

    required  this.selector,

     this.areaOverride,

    required  this.capture,

     this.effectiveCapture,

    required  this.enabled,

    required  this.addedAt,

     this.addedAtSeq,

     this.removedAt,

     this.estimate,

     this.stats,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



      /// ULID identifier
  @JsonKey(
    
    name: r'chronicleId',
    required: true,
    includeIfNull: false,
  )


  final String chronicleId;



  @JsonKey(
    
    name: r'name',
    required: true,
    includeIfNull: false,
  )


  final String name;



  @JsonKey(
    
    name: r'selector',
    required: true,
    includeIfNull: false,
  )


  final ChronicleLayerSelector selector;



  @JsonKey(
    
    name: r'areaOverride',
    required: false,
    includeIfNull: false,
  )


  final ChronicleArea? areaOverride;



  @JsonKey(
    
    name: r'capture',
    required: true,
    includeIfNull: false,
  )


  final CaptureMode capture;



  @JsonKey(
    
    name: r'effectiveCapture',
    required: false,
    includeIfNull: false,
  )


  final CaptureMode? effectiveCapture;



  @JsonKey(
    
    name: r'enabled',
    required: true,
    includeIfNull: false,
  )


  final bool enabled;



  @JsonKey(
    
    name: r'addedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime addedAt;



      /// Edits apply from this seq onward
  @JsonKey(
    
    name: r'addedAtSeq',
    required: false,
    includeIfNull: false,
  )


  final int? addedAtSeq;



  @JsonKey(
    
    name: r'removedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? removedAt;



  @JsonKey(
    
    name: r'estimate',
    required: false,
    includeIfNull: false,
  )


  final ChronicleLayerEstimate? estimate;



  @JsonKey(
    
    name: r'stats',
    required: false,
    includeIfNull: false,
  )


  final ChronicleLayerStats? stats;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ChronicleLayer &&
      other.id == id &&
      other.chronicleId == chronicleId &&
      other.name == name &&
      other.selector == selector &&
      other.areaOverride == areaOverride &&
      other.capture == capture &&
      other.effectiveCapture == effectiveCapture &&
      other.enabled == enabled &&
      other.addedAt == addedAt &&
      other.addedAtSeq == addedAtSeq &&
      other.removedAt == removedAt &&
      other.estimate == estimate &&
      other.stats == stats;

    @override
    int get hashCode =>
        id.hashCode +
        chronicleId.hashCode +
        name.hashCode +
        selector.hashCode +
        (areaOverride == null ? 0 : areaOverride.hashCode) +
        capture.hashCode +
        effectiveCapture.hashCode +
        enabled.hashCode +
        addedAt.hashCode +
        (addedAtSeq == null ? 0 : addedAtSeq.hashCode) +
        (removedAt == null ? 0 : removedAt.hashCode) +
        (estimate == null ? 0 : estimate.hashCode) +
        stats.hashCode;

  factory ChronicleLayer.fromJson(Map<String, dynamic> json) => _$ChronicleLayerFromJson(json);

  Map<String, dynamic> toJson() => _$ChronicleLayerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

