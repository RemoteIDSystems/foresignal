//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'pol_subject_input_params.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PolSubjectInputParams {
  /// Returns a new [PolSubjectInputParams] instance.
  PolSubjectInputParams({

     this.stayRadiusM = 100,

     this.minDwell = '10m',

     this.maxGap = '30m',

     this.eps = 150,

     this.minPts = 3,

     this.lookback = '28d',
  });

          // minimum: 1
  @JsonKey(
    defaultValue: 100,
    name: r'stayRadiusM',
    required: false,
    includeIfNull: false,
  )


  final num? stayRadiusM;



      /// Minimum dwell for a stay. ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h).
  @JsonKey(
    defaultValue: '10m',
    name: r'minDwell',
    required: false,
    includeIfNull: false,
  )


  final String? minDwell;



      /// Gap that ends a stay. ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h).
  @JsonKey(
    defaultValue: '30m',
    name: r'maxGap',
    required: false,
    includeIfNull: false,
  )


  final String? maxGap;



      /// DBSCAN eps in metres
          // minimum: 1
  @JsonKey(
    defaultValue: 150,
    name: r'eps',
    required: false,
    includeIfNull: false,
  )


  final num? eps;



      /// DBSCAN minimum stays
          // minimum: 1
  @JsonKey(
    defaultValue: 3,
    name: r'minPts',
    required: false,
    includeIfNull: false,
  )


  final int? minPts;



      /// Nightly recompute window over stored stays. ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h).
  @JsonKey(
    defaultValue: '28d',
    name: r'lookback',
    required: false,
    includeIfNull: false,
  )


  final String? lookback;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PolSubjectInputParams &&
      other.stayRadiusM == stayRadiusM &&
      other.minDwell == minDwell &&
      other.maxGap == maxGap &&
      other.eps == eps &&
      other.minPts == minPts &&
      other.lookback == lookback;

    @override
    int get hashCode =>
        stayRadiusM.hashCode +
        minDwell.hashCode +
        maxGap.hashCode +
        eps.hashCode +
        minPts.hashCode +
        lookback.hashCode;

  factory PolSubjectInputParams.fromJson(Map<String, dynamic> json) => _$PolSubjectInputParamsFromJson(json);

  Map<String, dynamic> toJson() => _$PolSubjectInputParamsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

