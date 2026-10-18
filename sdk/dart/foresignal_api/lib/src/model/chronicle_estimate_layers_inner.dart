//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/capture_mode.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'chronicle_estimate_layers_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ChronicleEstimateLayersInner {
  /// Returns a new [ChronicleEstimateLayersInner] instance.
  ChronicleEstimateLayersInner({

    required  this.layerId,

     this.name,

     this.capture,

    required  this.rowsPerDay,

    required  this.bytesPerDay,

     this.basis,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'layerId',
    required: true,
    includeIfNull: false,
  )


  final String layerId;



  @JsonKey(
    
    name: r'name',
    required: false,
    includeIfNull: false,
  )


  final String? name;



  @JsonKey(
    
    name: r'capture',
    required: false,
    includeIfNull: false,
  )


  final CaptureMode? capture;



  @JsonKey(
    
    name: r'rowsPerDay',
    required: true,
    includeIfNull: false,
  )


  final num rowsPerDay;



  @JsonKey(
    
    name: r'bytesPerDay',
    required: true,
    includeIfNull: false,
  )


  final num bytesPerDay;



  @JsonKey(
    
    name: r'basis',
    required: false,
    includeIfNull: false,
  )


  final String? basis;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ChronicleEstimateLayersInner &&
      other.layerId == layerId &&
      other.name == name &&
      other.capture == capture &&
      other.rowsPerDay == rowsPerDay &&
      other.bytesPerDay == bytesPerDay &&
      other.basis == basis;

    @override
    int get hashCode =>
        layerId.hashCode +
        name.hashCode +
        capture.hashCode +
        rowsPerDay.hashCode +
        bytesPerDay.hashCode +
        (basis == null ? 0 : basis.hashCode);

  factory ChronicleEstimateLayersInner.fromJson(Map<String, dynamic> json) => _$ChronicleEstimateLayersInnerFromJson(json);

  Map<String, dynamic> toJson() => _$ChronicleEstimateLayersInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

