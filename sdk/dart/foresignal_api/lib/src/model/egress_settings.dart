//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'egress_settings.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class EgressSettings {
  /// Returns a new [EgressSettings] instance.
  EgressSettings({

     this.rotation = 'direct',

     this.defaultRoute,
  });

      /// How outbound requests pick a route; direct means no egress routing at all: pins and the default route are ignored and every pull goes direct (R2-018)
  @JsonKey(
    defaultValue: 'direct',
    name: r'rotation',
    required: true,
    includeIfNull: false,
  )


  final String rotation;



      /// With rotation on, the route a source excluded from rotation uses (an excluded source with no default route goes direct); it must name an existing route (R2-018)
  @JsonKey(
    
    name: r'defaultRoute',
    required: false,
    includeIfNull: false,
  )


  final String? defaultRoute;





    @override
    bool operator ==(Object other) => identical(this, other) || other is EgressSettings &&
      other.rotation == rotation &&
      other.defaultRoute == defaultRoute;

    @override
    int get hashCode =>
        rotation.hashCode +
        (defaultRoute == null ? 0 : defaultRoute.hashCode);

  factory EgressSettings.fromJson(Map<String, dynamic> json) => _$EgressSettingsFromJson(json);

  Map<String, dynamic> toJson() => _$EgressSettingsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

