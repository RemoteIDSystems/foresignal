//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/edp_manifest.dart';
import 'package:foresignal_api/src/model/external_source_pull.dart';
import 'package:foresignal_api/src/model/source_access.dart';
import 'package:foresignal_api/src/model/edp_presentation.dart';
import 'package:foresignal_api/src/model/external_stream_stats.dart';
import 'package:foresignal_api/src/model/edp_item_rejection.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'external_source.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ExternalSource {
  /// Returns a new [ExternalSource] instance.
  ExternalSource({

    required  this.sourceId,

    required  this.state,

     this.registeredVia,

    required  this.manifest,

     this.manifestSha256,

     this.access,

     this.egress,

     this.approvedBy,

     this.approvedAt,

    required  this.createdAt,

     this.lastItemAt,

     this.pull,

     this.streams,

     this.lastValidationErrors,

     this.health,

     this.allowPrivateNetwork,

     this.presentation,

     this.breakingChanges,
  });

  @JsonKey(
    
    name: r'sourceId',
    required: true,
    includeIfNull: false,
  )


  final String sourceId;



  @JsonKey(
    
    name: r'state',
    required: true,
    includeIfNull: false,
  )


  final ExternalSourceStateEnum state;



  @JsonKey(
    
    name: r'registeredVia',
    required: false,
    includeIfNull: false,
  )


  final ExternalSourceRegisteredViaEnum? registeredVia;



  @JsonKey(
    
    name: r'manifest',
    required: true,
    includeIfNull: false,
  )


  final EdpManifest manifest;



  @JsonKey(
    
    name: r'manifestSha256',
    required: false,
    includeIfNull: false,
  )


  final String? manifestSha256;



  @JsonKey(
    
    name: r'access',
    required: false,
    includeIfNull: false,
  )


  final SourceAccess? access;



  @JsonKey(
    
    name: r'egress',
    required: false,
    includeIfNull: false,
  )


  final String? egress;



  @JsonKey(
    
    name: r'approvedBy',
    required: false,
    includeIfNull: false,
  )


  final String? approvedBy;



  @JsonKey(
    
    name: r'approvedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? approvedAt;



  @JsonKey(
    
    name: r'createdAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime createdAt;



  @JsonKey(
    
    name: r'lastItemAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? lastItemAt;



  @JsonKey(
    
    name: r'pull',
    required: false,
    includeIfNull: false,
  )


  final ExternalSourcePull? pull;



  @JsonKey(
    
    name: r'streams',
    required: false,
    includeIfNull: false,
  )


  final List<ExternalStreamStats>? streams;



  @JsonKey(
    
    name: r'lastValidationErrors',
    required: false,
    includeIfNull: false,
  )


  final List<EdpItemRejection>? lastValidationErrors;



  @JsonKey(
    
    name: r'health',
    required: false,
    includeIfNull: false,
  )


  final String? health;



      /// Whether provider URLs may be on a private network (set at registration)
  @JsonKey(
    
    name: r'allowPrivateNetwork',
    required: false,
    includeIfNull: false,
  )


  final bool? allowPrivateNetwork;



      /// Layer presentation hints of the layer streams, keyed by stream id; also in each kind's TileJSON as foresignal:presentation
  @JsonKey(
    
    name: r'presentation',
    required: false,
    includeIfNull: false,
  )


  final Map<String, EdpPresentation>? presentation;



      /// updateExternalSourceManifest only: the changes that returned the source to pending (empty for an additive update)
  @JsonKey(
    
    name: r'breakingChanges',
    required: false,
    includeIfNull: false,
  )


  final List<String>? breakingChanges;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ExternalSource &&
      other.sourceId == sourceId &&
      other.state == state &&
      other.registeredVia == registeredVia &&
      other.manifest == manifest &&
      other.manifestSha256 == manifestSha256 &&
      other.access == access &&
      other.egress == egress &&
      other.approvedBy == approvedBy &&
      other.approvedAt == approvedAt &&
      other.createdAt == createdAt &&
      other.lastItemAt == lastItemAt &&
      other.pull == pull &&
      other.streams == streams &&
      other.lastValidationErrors == lastValidationErrors &&
      other.health == health &&
      other.allowPrivateNetwork == allowPrivateNetwork &&
      other.presentation == presentation &&
      other.breakingChanges == breakingChanges;

    @override
    int get hashCode =>
        sourceId.hashCode +
        state.hashCode +
        registeredVia.hashCode +
        manifest.hashCode +
        manifestSha256.hashCode +
        access.hashCode +
        egress.hashCode +
        (approvedBy == null ? 0 : approvedBy.hashCode) +
        (approvedAt == null ? 0 : approvedAt.hashCode) +
        createdAt.hashCode +
        (lastItemAt == null ? 0 : lastItemAt.hashCode) +
        (pull == null ? 0 : pull.hashCode) +
        streams.hashCode +
        lastValidationErrors.hashCode +
        health.hashCode +
        allowPrivateNetwork.hashCode +
        presentation.hashCode +
        breakingChanges.hashCode;

  factory ExternalSource.fromJson(Map<String, dynamic> json) => _$ExternalSourceFromJson(json);

  Map<String, dynamic> toJson() => _$ExternalSourceToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ExternalSourceStateEnum {
@JsonValue(r'pending')
pending(r'pending'),
@JsonValue(r'active')
active(r'active'),
@JsonValue(r'suspended')
suspended(r'suspended');

const ExternalSourceStateEnum(this.value);

final String value;

@override
String toString() => value;
}


enum ExternalSourceRegisteredViaEnum {
@JsonValue(r'operator')
operator_(r'operator'),
@JsonValue(r'registration-token')
registrationToken(r'registration-token');

const ExternalSourceRegisteredViaEnum(this.value);

final String value;

@override
String toString() => value;
}


