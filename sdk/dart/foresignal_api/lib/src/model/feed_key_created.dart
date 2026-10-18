//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/read_restriction.dart';
import 'package:foresignal_api/src/model/feed_key_created_all_of_urls.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'feed_key_created.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class FeedKeyCreated {
  /// Returns a new [FeedKeyCreated] instance.
  FeedKeyCreated({

    required  this.id,

    required  this.name,

    required  this.keyPrefix,

    required  this.prefixes,

    required  this.restrictions,

     this.privateAccess,

    required  this.createdAt,

    required  this.expiresAt,

     this.lastUsedAt,

     this.revokedAt,

    required  this.key,

    required  this.urls,
  });

  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'name',
    required: true,
    includeIfNull: false,
  )


  final String name;



  @JsonKey(
    
    name: r'keyPrefix',
    required: true,
    includeIfNull: false,
  )


  final String keyPrefix;



  @JsonKey(
    
    name: r'prefixes',
    required: true,
    includeIfNull: false,
  )


  final List<String> prefixes;



  @JsonKey(
    
    name: r'restrictions',
    required: true,
    includeIfNull: false,
  )


  final ReadRestriction restrictions;



  @JsonKey(
    
    name: r'privateAccess',
    required: false,
    includeIfNull: false,
  )


  final bool? privateAccess;



  @JsonKey(
    
    name: r'createdAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime createdAt;



  @JsonKey(
    
    name: r'expiresAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime expiresAt;



  @JsonKey(
    
    name: r'lastUsedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? lastUsedAt;



  @JsonKey(
    
    name: r'revokedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? revokedAt;



      /// `fsf_…`; shown once
  @JsonKey(
    
    name: r'key',
    required: true,
    includeIfNull: false,
  )


  final String key;



  @JsonKey(
    
    name: r'urls',
    required: true,
    includeIfNull: false,
  )


  final List<FeedKeyCreatedAllOfUrls> urls;





    @override
    bool operator ==(Object other) => identical(this, other) || other is FeedKeyCreated &&
      other.id == id &&
      other.name == name &&
      other.keyPrefix == keyPrefix &&
      other.prefixes == prefixes &&
      other.restrictions == restrictions &&
      other.privateAccess == privateAccess &&
      other.createdAt == createdAt &&
      other.expiresAt == expiresAt &&
      other.lastUsedAt == lastUsedAt &&
      other.revokedAt == revokedAt &&
      other.key == key &&
      other.urls == urls;

    @override
    int get hashCode =>
        id.hashCode +
        name.hashCode +
        keyPrefix.hashCode +
        prefixes.hashCode +
        restrictions.hashCode +
        privateAccess.hashCode +
        createdAt.hashCode +
        expiresAt.hashCode +
        (lastUsedAt == null ? 0 : lastUsedAt.hashCode) +
        (revokedAt == null ? 0 : revokedAt.hashCode) +
        key.hashCode +
        urls.hashCode;

  factory FeedKeyCreated.fromJson(Map<String, dynamic> json) => _$FeedKeyCreatedFromJson(json);

  Map<String, dynamic> toJson() => _$FeedKeyCreatedToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

