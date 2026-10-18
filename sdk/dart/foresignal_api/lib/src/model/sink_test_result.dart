//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'sink_test_result.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SinkTestResult {
  /// Returns a new [SinkTestResult] instance.
  SinkTestResult({

    required  this.ok,

    required  this.phase,

    required  this.message,

     this.latencyMs,

     this.details,
  });

  @JsonKey(
    
    name: r'ok',
    required: true,
    includeIfNull: false,
  )


  final bool ok;



      /// Last phase reached
  @JsonKey(
    
    name: r'phase',
    required: true,
    includeIfNull: false,
  )


  final SinkTestResultPhaseEnum phase;



  @JsonKey(
    
    name: r'message',
    required: true,
    includeIfNull: false,
  )


  final String message;



  @JsonKey(
    
    name: r'latencyMs',
    required: false,
    includeIfNull: false,
  )


  final int? latencyMs;



  @JsonKey(
    
    name: r'details',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? details;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SinkTestResult &&
      other.ok == ok &&
      other.phase == phase &&
      other.message == message &&
      other.latencyMs == latencyMs &&
      other.details == details;

    @override
    int get hashCode =>
        ok.hashCode +
        phase.hashCode +
        message.hashCode +
        (latencyMs == null ? 0 : latencyMs.hashCode) +
        details.hashCode;

  factory SinkTestResult.fromJson(Map<String, dynamic> json) => _$SinkTestResultFromJson(json);

  Map<String, dynamic> toJson() => _$SinkTestResultToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// Last phase reached
enum SinkTestResultPhaseEnum {
@JsonValue(r'validate')
validate(r'validate'),
@JsonValue(r'connect')
connect(r'connect'),
@JsonValue(r'bind')
bind(r'bind'),
@JsonValue(r'send')
send(r'send');

const SinkTestResultPhaseEnum(this.value);

final String value;

@override
String toString() => value;
}


