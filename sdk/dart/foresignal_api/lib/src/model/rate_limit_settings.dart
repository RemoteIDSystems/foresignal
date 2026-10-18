//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/rate_limit_settings_stream.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'rate_limit_settings.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class RateLimitSettings {
  /// Returns a new [RateLimitSettings] instance.
  RateLimitSettings({

     this.read = 600,

     this.tiles = 1200,

     this.stream,

     this.expensive = 20,

     this.write = 120,

     this.anonymous = 120,

     this.metricsPerMinute,
  });

          // minimum: 1
  @JsonKey(
    defaultValue: 600,
    name: r'read',
    required: false,
    includeIfNull: false,
  )


  final int? read;



          // minimum: 1
  @JsonKey(
    defaultValue: 1200,
    name: r'tiles',
    required: false,
    includeIfNull: false,
  )


  final int? tiles;



  @JsonKey(
    
    name: r'stream',
    required: false,
    includeIfNull: false,
  )


  final RateLimitSettingsStream? stream;



      /// search, Ask, LLM-backed MCP tools, /v1/sample
          // minimum: 1
  @JsonKey(
    defaultValue: 20,
    name: r'expensive',
    required: false,
    includeIfNull: false,
  )


  final int? expensive;



          // minimum: 1
  @JsonKey(
    defaultValue: 120,
    name: r'write',
    required: false,
    includeIfNull: false,
  )


  final int? write;



      /// Per IP
          // minimum: 1
  @JsonKey(
    defaultValue: 120,
    name: r'anonymous',
    required: false,
    includeIfNull: false,
  )


  final int? anonymous;



      /// Optional limit for /actuator/prometheus by IP
          // minimum: 1
  @JsonKey(
    
    name: r'metricsPerMinute',
    required: false,
    includeIfNull: false,
  )


  final int? metricsPerMinute;





    @override
    bool operator ==(Object other) => identical(this, other) || other is RateLimitSettings &&
      other.read == read &&
      other.tiles == tiles &&
      other.stream == stream &&
      other.expensive == expensive &&
      other.write == write &&
      other.anonymous == anonymous &&
      other.metricsPerMinute == metricsPerMinute;

    @override
    int get hashCode =>
        read.hashCode +
        tiles.hashCode +
        stream.hashCode +
        expensive.hashCode +
        write.hashCode +
        anonymous.hashCode +
        (metricsPerMinute == null ? 0 : metricsPerMinute.hashCode);

  factory RateLimitSettings.fromJson(Map<String, dynamic> json) => _$RateLimitSettingsFromJson(json);

  Map<String, dynamic> toJson() => _$RateLimitSettingsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

