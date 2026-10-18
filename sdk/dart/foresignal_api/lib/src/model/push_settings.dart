//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'push_settings.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PushSettings {
  /// Returns a new [PushSettings] instance.
  PushSettings({

     this.contact = 'mailto:admin@localhost',

     this.allowedPrivateHosts,

     this.defaultLang = 'en',
  });

      /// VAPID `sub`
  @JsonKey(
    defaultValue: 'mailto:admin@localhost',
    name: r'contact',
    required: false,
    includeIfNull: false,
  )


  final String? contact;



      /// Exact hostnames or CIDRs (IPv4 prefix ≥ /8, IPv6 ≥ /32) allowed as push endpoints despite resolving to private addresses (e.g. a LAN UnifiedPush distributor); https still required
  @JsonKey(
    
    name: r'allowedPrivateHosts',
    required: false,
    includeIfNull: false,
  )


  final List<String>? allowedPrivateHosts;



  @JsonKey(
    defaultValue: 'en',
    name: r'defaultLang',
    required: false,
    includeIfNull: false,
  )


  final String? defaultLang;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PushSettings &&
      other.contact == contact &&
      other.allowedPrivateHosts == allowedPrivateHosts &&
      other.defaultLang == defaultLang;

    @override
    int get hashCode =>
        contact.hashCode +
        allowedPrivateHosts.hashCode +
        defaultLang.hashCode;

  factory PushSettings.fromJson(Map<String, dynamic> json) => _$PushSettingsFromJson(json);

  Map<String, dynamic> toJson() => _$PushSettingsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

