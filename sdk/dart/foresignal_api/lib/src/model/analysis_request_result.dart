//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'analysis_request_result.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AnalysisRequestResult {
  /// Returns a new [AnalysisRequestResult] instance.
  AnalysisRequestResult({

    required  this.correlationId,

    required  this.recorded,

     this.forecastId,
  });

      /// The id sent to the provider and echoed in its callback.
  @JsonKey(
    
    name: r'correlationId',
    required: true,
    includeIfNull: false,
  )


  final String correlationId;



      /// True when a forecast was recorded at once (a synchronous provider, or a callback that resolved).
  @JsonKey(
    
    name: r'recorded',
    required: true,
    includeIfNull: false,
  )


  final bool recorded;



      /// The recorded forecast's id, present when recorded is true via the callback.
  @JsonKey(
    
    name: r'forecastId',
    required: false,
    includeIfNull: false,
  )


  final String? forecastId;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AnalysisRequestResult &&
      other.correlationId == correlationId &&
      other.recorded == recorded &&
      other.forecastId == forecastId;

    @override
    int get hashCode =>
        correlationId.hashCode +
        recorded.hashCode +
        forecastId.hashCode;

  factory AnalysisRequestResult.fromJson(Map<String, dynamic> json) => _$AnalysisRequestResultFromJson(json);

  Map<String, dynamic> toJson() => _$AnalysisRequestResultToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

