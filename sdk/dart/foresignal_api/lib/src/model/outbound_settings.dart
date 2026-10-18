//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'outbound_settings.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class OutboundSettings {
  /// Returns a new [OutboundSettings] instance.
  OutboundSettings({

     this.allowHosts,

     this.allowCidrs,

     this.denyHosts,

     this.denyCidrs,
  });

      /// Hosts allowed even when they resolve to addresses the profile would refuse (never the always-blocked set)
  @JsonKey(
    
    name: r'allowHosts',
    required: false,
    includeIfNull: false,
  )


  final List<String>? allowHosts;



      /// Address ranges allowed in every profile (never the always-blocked set)
  @JsonKey(
    
    name: r'allowCidrs',
    required: false,
    includeIfNull: false,
  )


  final List<String>? allowCidrs;



      /// Hosts refused in every profile
  @JsonKey(
    
    name: r'denyHosts',
    required: false,
    includeIfNull: false,
  )


  final List<String>? denyHosts;



      /// Address ranges refused in every profile
  @JsonKey(
    
    name: r'denyCidrs',
    required: false,
    includeIfNull: false,
  )


  final List<String>? denyCidrs;





    @override
    bool operator ==(Object other) => identical(this, other) || other is OutboundSettings &&
      other.allowHosts == allowHosts &&
      other.allowCidrs == allowCidrs &&
      other.denyHosts == denyHosts &&
      other.denyCidrs == denyCidrs;

    @override
    int get hashCode =>
        allowHosts.hashCode +
        allowCidrs.hashCode +
        denyHosts.hashCode +
        denyCidrs.hashCode;

  factory OutboundSettings.fromJson(Map<String, dynamic> json) => _$OutboundSettingsFromJson(json);

  Map<String, dynamic> toJson() => _$OutboundSettingsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

