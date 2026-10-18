//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/edp_manifest.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'external_source_registration.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ExternalSourceRegistration {
  /// Returns a new [ExternalSourceRegistration] instance.
  ExternalSourceRegistration({

     this.manifest,

     this.manifestUrl,

     this.approve = false,

     this.allowPrivateNetwork = false,
  });

  @JsonKey(
    
    name: r'manifest',
    required: false,
    includeIfNull: false,
  )


  final EdpManifest? manifest;



  @JsonKey(
    
    name: r'manifestUrl',
    required: false,
    includeIfNull: false,
  )


  final String? manifestUrl;



  @JsonKey(
    defaultValue: false,
    name: r'approve',
    required: false,
    includeIfNull: false,
  )


  final bool? approve;



      /// Allow this provider's URLs (manifestUrl, pull itemsUrl, search url) to resolve to private, loopback, link-local or carrier-grade NAT addresses and to use plain http, for a provider on the operator's own network. Cloud-metadata addresses stay refused (EDP §7, invariant EXT-6). Set only at registration (024).
  @JsonKey(
    defaultValue: false,
    name: r'allowPrivateNetwork',
    required: false,
    includeIfNull: false,
  )


  final bool? allowPrivateNetwork;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ExternalSourceRegistration &&
      other.manifest == manifest &&
      other.manifestUrl == manifestUrl &&
      other.approve == approve &&
      other.allowPrivateNetwork == allowPrivateNetwork;

    @override
    int get hashCode =>
        (manifest == null ? 0 : manifest.hashCode) +
        (manifestUrl == null ? 0 : manifestUrl.hashCode) +
        approve.hashCode +
        allowPrivateNetwork.hashCode;

  factory ExternalSourceRegistration.fromJson(Map<String, dynamic> json) => _$ExternalSourceRegistrationFromJson(json);

  Map<String, dynamic> toJson() => _$ExternalSourceRegistrationToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

