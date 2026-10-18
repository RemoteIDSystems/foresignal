//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/caveat.dart';
import 'package:foresignal_api/src/model/chronicle_estimate_layers_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'chronicle_estimate.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ChronicleEstimate {
  /// Returns a new [ChronicleEstimate] instance.
  ChronicleEstimate({

    required  this.chronicleId,

    required  this.computedAt,

    required  this.layers,

    required  this.totalRowsPerDay,

    required  this.totalBytesPerDay,

    required  this.sizeCapBytes,

     this.bytesUsed,

     this.daysToCap,

     this.caveats,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'chronicleId',
    required: true,
    includeIfNull: false,
  )


  final String chronicleId;



  @JsonKey(
    
    name: r'computedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime computedAt;



  @JsonKey(
    
    name: r'layers',
    required: true,
    includeIfNull: false,
  )


  final List<ChronicleEstimateLayersInner> layers;



  @JsonKey(
    
    name: r'totalRowsPerDay',
    required: true,
    includeIfNull: false,
  )


  final num totalRowsPerDay;



  @JsonKey(
    
    name: r'totalBytesPerDay',
    required: true,
    includeIfNull: false,
  )


  final num totalBytesPerDay;



  @JsonKey(
    
    name: r'sizeCapBytes',
    required: true,
    includeIfNull: false,
  )


  final int sizeCapBytes;



  @JsonKey(
    
    name: r'bytesUsed',
    required: false,
    includeIfNull: false,
  )


  final int? bytesUsed;



  @JsonKey(
    
    name: r'daysToCap',
    required: false,
    includeIfNull: false,
  )


  final num? daysToCap;



  @JsonKey(
    
    name: r'caveats',
    required: false,
    includeIfNull: false,
  )


  final List<Caveat>? caveats;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ChronicleEstimate &&
      other.chronicleId == chronicleId &&
      other.computedAt == computedAt &&
      other.layers == layers &&
      other.totalRowsPerDay == totalRowsPerDay &&
      other.totalBytesPerDay == totalBytesPerDay &&
      other.sizeCapBytes == sizeCapBytes &&
      other.bytesUsed == bytesUsed &&
      other.daysToCap == daysToCap &&
      other.caveats == caveats;

    @override
    int get hashCode =>
        chronicleId.hashCode +
        computedAt.hashCode +
        layers.hashCode +
        totalRowsPerDay.hashCode +
        totalBytesPerDay.hashCode +
        sizeCapBytes.hashCode +
        bytesUsed.hashCode +
        (daysToCap == null ? 0 : daysToCap.hashCode) +
        caveats.hashCode;

  factory ChronicleEstimate.fromJson(Map<String, dynamic> json) => _$ChronicleEstimateFromJson(json);

  Map<String, dynamic> toJson() => _$ChronicleEstimateToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

