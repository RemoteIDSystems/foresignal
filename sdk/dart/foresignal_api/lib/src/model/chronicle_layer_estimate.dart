//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'chronicle_layer_estimate.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ChronicleLayerEstimate {
  /// Returns a new [ChronicleLayerEstimate] instance.
  ChronicleLayerEstimate({

    required  this.rowsPerDay,

    required  this.bytesPerDay,

     this.basis,
  });

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



      /// How the projection was computed, e.g. last 7 d of global history
  @JsonKey(
    
    name: r'basis',
    required: false,
    includeIfNull: false,
  )


  final String? basis;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ChronicleLayerEstimate &&
      other.rowsPerDay == rowsPerDay &&
      other.bytesPerDay == bytesPerDay &&
      other.basis == basis;

    @override
    int get hashCode =>
        rowsPerDay.hashCode +
        bytesPerDay.hashCode +
        (basis == null ? 0 : basis.hashCode);

  factory ChronicleLayerEstimate.fromJson(Map<String, dynamic> json) => _$ChronicleLayerEstimateFromJson(json);

  Map<String, dynamic> toJson() => _$ChronicleLayerEstimateToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

