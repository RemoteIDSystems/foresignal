//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/principal_summary.dart';
import 'package:foresignal_api/src/model/saved_search_query.dart';
import 'package:foresignal_api/src/model/visibility.dart';
import 'package:foresignal_api/src/model/tlp.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'saved_search.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SavedSearch {
  /// Returns a new [SavedSearch] instance.
  SavedSearch({

    required  this.id,

    required  this.name,

    required  this.owner,

    required  this.query,

    required  this.visibility,

    required  this.tlp,

    required  this.notify,

     this.lastRunAt,

     this.lastNotifiedAt,

     this.lastMatchCount,

    required  this.createdAt,

     this.updatedAt,
  });

      /// ULID identifier
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
    
    name: r'owner',
    required: true,
    includeIfNull: false,
  )


  final PrincipalSummary owner;



  @JsonKey(
    
    name: r'query',
    required: true,
    includeIfNull: false,
  )


  final SavedSearchQuery query;



  @JsonKey(
    
    name: r'visibility',
    required: true,
    includeIfNull: false,
  )


  final Visibility visibility;



  @JsonKey(
    
    name: r'tlp',
    required: true,
    includeIfNull: false,
  )


  final Tlp tlp;



  @JsonKey(
    
    name: r'notify',
    required: true,
    includeIfNull: false,
  )


  final bool notify;



  @JsonKey(
    
    name: r'lastRunAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? lastRunAt;



  @JsonKey(
    
    name: r'lastNotifiedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? lastNotifiedAt;



  @JsonKey(
    
    name: r'lastMatchCount',
    required: false,
    includeIfNull: false,
  )


  final int? lastMatchCount;



  @JsonKey(
    
    name: r'createdAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime createdAt;



  @JsonKey(
    
    name: r'updatedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? updatedAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SavedSearch &&
      other.id == id &&
      other.name == name &&
      other.owner == owner &&
      other.query == query &&
      other.visibility == visibility &&
      other.tlp == tlp &&
      other.notify == notify &&
      other.lastRunAt == lastRunAt &&
      other.lastNotifiedAt == lastNotifiedAt &&
      other.lastMatchCount == lastMatchCount &&
      other.createdAt == createdAt &&
      other.updatedAt == updatedAt;

    @override
    int get hashCode =>
        id.hashCode +
        name.hashCode +
        owner.hashCode +
        query.hashCode +
        visibility.hashCode +
        tlp.hashCode +
        notify.hashCode +
        (lastRunAt == null ? 0 : lastRunAt.hashCode) +
        (lastNotifiedAt == null ? 0 : lastNotifiedAt.hashCode) +
        (lastMatchCount == null ? 0 : lastMatchCount.hashCode) +
        createdAt.hashCode +
        updatedAt.hashCode;

  factory SavedSearch.fromJson(Map<String, dynamic> json) => _$SavedSearchFromJson(json);

  Map<String, dynamic> toJson() => _$SavedSearchToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

