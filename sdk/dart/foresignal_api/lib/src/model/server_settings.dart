//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/client_links.dart';
import 'package:foresignal_api/src/model/push_settings.dart';
import 'package:foresignal_api/src/model/settings_warning.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'server_settings.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ServerSettings {
  /// Returns a new [ServerSettings] instance.
  ServerSettings({

    required  this.publicBaseUrl,

     this.timezone = 'UTC',

     this.clientLinks,

     this.warnings,

     this.push,

     this.contactEmail,
  });

      /// Base URL used in notification links; null uses the request's own scheme, host and port
  @JsonKey(
    
    name: r'publicBaseUrl',
    required: true,
    includeIfNull: true,
  )


  final String? publicBaseUrl;



      /// IANA zone; fallback for channel timezones
  @JsonKey(
    defaultValue: 'UTC',
    name: r'timezone',
    required: false,
    includeIfNull: false,
  )


  final String? timezone;



  @JsonKey(
    
    name: r'clientLinks',
    required: false,
    includeIfNull: false,
  )


  final ClientLinks? clientLinks;



      /// PUBLIC_BASE_URL_INSECURE when publicBaseUrl is http:// on a non-local host; PUBLIC_BASE_URL_MISMATCH when requests arrive over https
  @JsonKey(
    
    name: r'warnings',
    required: false,
    includeIfNull: false,
  )


  final List<SettingsWarning>? warnings;



  @JsonKey(
    
    name: r'push',
    required: false,
    includeIfNull: false,
  )


  final PushSettings? push;



      /// Operator contact appended to the upstream User-Agent as `contact=…` (the NWS API asks for a contact); null for none
  @JsonKey(
    
    name: r'contactEmail',
    required: false,
    includeIfNull: false,
  )


  final String? contactEmail;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ServerSettings &&
      other.publicBaseUrl == publicBaseUrl &&
      other.timezone == timezone &&
      other.clientLinks == clientLinks &&
      other.warnings == warnings &&
      other.push == push &&
      other.contactEmail == contactEmail;

    @override
    int get hashCode =>
        (publicBaseUrl == null ? 0 : publicBaseUrl.hashCode) +
        timezone.hashCode +
        clientLinks.hashCode +
        warnings.hashCode +
        push.hashCode +
        (contactEmail == null ? 0 : contactEmail.hashCode);

  factory ServerSettings.fromJson(Map<String, dynamic> json) => _$ServerSettingsFromJson(json);

  Map<String, dynamic> toJson() => _$ServerSettingsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

