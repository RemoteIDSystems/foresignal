//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'analysis_callback.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AnalysisCallback {
  /// Returns a new [AnalysisCallback] instance.
  AnalysisCallback({

    required  this.correlationId,

     this.probability,

     this.quantiles,
  });

      /// The correlation id from the original request; the credential for this callback.
  @JsonKey(
    
    name: r'correlationId',
    required: true,
    includeIfNull: false,
  )


  final String correlationId;



      /// The forecast probability for a binary question.
          // minimum: 0
          // maximum: 1
  @JsonKey(
    
    name: r'probability',
    required: false,
    includeIfNull: false,
  )


  final num? probability;



      /// Quantile levels to values for a numeric question. The ledger's required levels are p05, p25, p50, p75, p95; a decile provider (p10..p90, as TimesFM emits) is accepted and mapped binding-side onto the required set.
  @JsonKey(
    
    name: r'quantiles',
    required: false,
    includeIfNull: false,
  )


  final Map<String, num>? quantiles;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AnalysisCallback &&
      other.correlationId == correlationId &&
      other.probability == probability &&
      other.quantiles == quantiles;

    @override
    int get hashCode =>
        correlationId.hashCode +
        probability.hashCode +
        quantiles.hashCode;

  factory AnalysisCallback.fromJson(Map<String, dynamic> json) => _$AnalysisCallbackFromJson(json);

  Map<String, dynamic> toJson() => _$AnalysisCallbackToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

