//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'glance_sources.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class GlanceSources {
  /// Returns a new [GlanceSources] instance.
  GlanceSources({

     this.healthy,

     this.degraded,

     this.failing,
  });

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
    bool operator ==(Object other) => identical(this, other) || other is GlanceSources &&
      other.healthy == healthy &&
      other.degraded == degraded &&
      other.failing == failing;

    @override
    int get hashCode =>
        healthy.hashCode +
        degraded.hashCode +
        failing.hashCode;

  factory GlanceSources.fromJson(Map<String, dynamic> json) => _$GlanceSourcesFromJson(json);

  Map<String, dynamic> toJson() => _$GlanceSourcesToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

