//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'digest_notable_item.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class DigestNotableItem {
  /// Returns a new [DigestNotableItem] instance.
  DigestNotableItem({

    required  this.id,

     this.source_,

     this.externalId,

     this.kind,

    required  this.seq,

     this.title,

     this.live,

     this.observedAt,

     this.effectiveTime,

     this.rankValue,

     this.link,
  });

      /// <sourceId>:<externalId>
  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'source',
    required: false,
    includeIfNull: false,
  )


  final String? source_;



  @JsonKey(
    
    name: r'externalId',
    required: false,
    includeIfNull: false,
  )


  final String? externalId;



  @JsonKey(
    
    name: r'kind',
    required: false,
    includeIfNull: false,
  )


  final String? kind;



      /// Persisted head seq (cite with it)
  @JsonKey(
    
    name: r'seq',
    required: true,
    includeIfNull: false,
  )


  final int seq;



  @JsonKey(
    
    name: r'title',
    required: false,
    includeIfNull: false,
  )


  final String? title;



  @JsonKey(
    
    name: r'live',
    required: false,
    includeIfNull: false,
  )


  final bool? live;



  @JsonKey(
    
    name: r'observedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? observedAt;



  @JsonKey(
    
    name: r'effectiveTime',
    required: false,
    includeIfNull: false,
  )


  final DateTime? effectiveTime;



      /// Value of the descriptor's rankField when present
  @JsonKey(
    
    name: r'rankValue',
    required: false,
    includeIfNull: false,
  )


  final Object? rankValue;



      /// publicBaseUrl/v1/records/{source}/{externalId}
  @JsonKey(
    
    name: r'link',
    required: false,
    includeIfNull: false,
  )


  final String? link;





    @override
    bool operator ==(Object other) => identical(this, other) || other is DigestNotableItem &&
      other.id == id &&
      other.source_ == source_ &&
      other.externalId == externalId &&
      other.kind == kind &&
      other.seq == seq &&
      other.title == title &&
      other.live == live &&
      other.observedAt == observedAt &&
      other.effectiveTime == effectiveTime &&
      other.rankValue == rankValue &&
      other.link == link;

    @override
    int get hashCode =>
        id.hashCode +
        source_.hashCode +
        externalId.hashCode +
        kind.hashCode +
        seq.hashCode +
        (title == null ? 0 : title.hashCode) +
        live.hashCode +
        observedAt.hashCode +
        (effectiveTime == null ? 0 : effectiveTime.hashCode) +
        (rankValue == null ? 0 : rankValue.hashCode) +
        link.hashCode;

  factory DigestNotableItem.fromJson(Map<String, dynamic> json) => _$DigestNotableItemFromJson(json);

  Map<String, dynamic> toJson() => _$DigestNotableItemToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

