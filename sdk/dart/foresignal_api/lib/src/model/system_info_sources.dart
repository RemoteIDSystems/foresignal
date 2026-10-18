//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'system_info_sources.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SystemInfoSources {
  /// Returns a new [SystemInfoSources] instance.
  SystemInfoSources({

     this.total,

     this.enabled,

     this.healthy,

     this.degraded,

     this.failing,
  });

  @JsonKey(
    
    name: r'total',
    required: false,
    includeIfNull: false,
  )


  final int? total;



  @JsonKey(
    
    name: r'enabled',
    required: false,
    includeIfNull: false,
  )


  final int? enabled;



  @JsonKey(
    
    name: r'healthy',
    required: false,
    includeIfNull: false,
  )


  final int? healthy;



  @JsonKey(
    
    name: r'degraded',
    required: false,
    includeIfNull: false,
  )


  final int? degraded;



  @JsonKey(
    
    name: r'failing',
    required: false,
    includeIfNull: false,
  )


  final int? failing;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SystemInfoSources &&
      other.total == total &&
      other.enabled == enabled &&
      other.healthy == healthy &&
      other.degraded == degraded &&
      other.failing == failing;

    @override
    int get hashCode =>
        total.hashCode +
        enabled.hashCode +
        healthy.hashCode +
        degraded.hashCode +
        failing.hashCode;

  factory SystemInfoSources.fromJson(Map<String, dynamic> json) => _$SystemInfoSourcesFromJson(json);

  Map<String, dynamic> toJson() => _$SystemInfoSourcesToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

