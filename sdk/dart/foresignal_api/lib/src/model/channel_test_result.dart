//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'channel_test_result.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ChannelTestResult {
  /// Returns a new [ChannelTestResult] instance.
  ChannelTestResult({

    required  this.success,

     this.status,

     this.durationMs,

     this.error,
  });

  @JsonKey(
    
    name: r'success',
    required: true,
    includeIfNull: false,
  )


  final bool success;



  @JsonKey(
    
    name: r'status',
    required: false,
    includeIfNull: false,
  )


  final int? status;



  @JsonKey(
    
    name: r'durationMs',
    required: false,
    includeIfNull: false,
  )


  final int? durationMs;



  @JsonKey(
    
    name: r'error',
    required: false,
    includeIfNull: false,
  )


  final String? error;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ChannelTestResult &&
      other.success == success &&
      other.status == status &&
      other.durationMs == durationMs &&
      other.error == error;

    @override
    int get hashCode =>
        success.hashCode +
        (status == null ? 0 : status.hashCode) +
        durationMs.hashCode +
        (error == null ? 0 : error.hashCode);

  factory ChannelTestResult.fromJson(Map<String, dynamic> json) => _$ChannelTestResultFromJson(json);

  Map<String, dynamic> toJson() => _$ChannelTestResultToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

