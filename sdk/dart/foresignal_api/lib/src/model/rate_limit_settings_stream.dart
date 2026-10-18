//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'rate_limit_settings_stream.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class RateLimitSettingsStream {
  /// Returns a new [RateLimitSettingsStream] instance.
  RateLimitSettingsStream({

     this.opensPerMinute = 30,

     this.concurrent = 8,
  });

          // minimum: 1
  @JsonKey(
    defaultValue: 30,
    name: r'opensPerMinute',
    required: false,
    includeIfNull: false,
  )


  final int? opensPerMinute;



          // minimum: 1
  @JsonKey(
    defaultValue: 8,
    name: r'concurrent',
    required: false,
    includeIfNull: false,
  )


  final int? concurrent;





    @override
    bool operator ==(Object other) => identical(this, other) || other is RateLimitSettingsStream &&
      other.opensPerMinute == opensPerMinute &&
      other.concurrent == concurrent;

    @override
    int get hashCode =>
        opensPerMinute.hashCode +
        concurrent.hashCode;

  factory RateLimitSettingsStream.fromJson(Map<String, dynamic> json) => _$RateLimitSettingsStreamFromJson(json);

  Map<String, dynamic> toJson() => _$RateLimitSettingsStreamToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

