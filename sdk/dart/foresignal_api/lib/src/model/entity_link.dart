//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'entity_link.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class EntityLink {
  /// Returns a new [EntityLink] instance.
  EntityLink({

    required  this.sourceId,

    required  this.externalId,

    required  this.keyType,

    required  this.keyValue,

    required  this.confidence,

    required  this.validFrom,

     this.validTo,

     this.firstSeq,
  });

  @JsonKey(
    
    name: r'sourceId',
    required: true,
    includeIfNull: false,
  )


  final String sourceId;



  @JsonKey(
    
    name: r'externalId',
    required: true,
    includeIfNull: false,
  )


  final String externalId;



      /// Expected to grow (`x-extensible-enum`): a client must accept a value not listed here.
  @JsonKey(
    
    name: r'keyType',
    required: true,
    includeIfNull: false,
  )


  final String keyType;



  @JsonKey(
    
    name: r'keyValue',
    required: true,
    includeIfNull: false,
  )


  final String keyValue;



          // minimum: 0
          // maximum: 1
  @JsonKey(
    
    name: r'confidence',
    required: true,
    includeIfNull: false,
  )


  final num confidence;



  @JsonKey(
    
    name: r'validFrom',
    required: true,
    includeIfNull: false,
  )


  final DateTime validFrom;



      /// null while the link is open
  @JsonKey(
    
    name: r'validTo',
    required: false,
    includeIfNull: false,
  )


  final DateTime? validTo;



      /// seq of the first linked version
  @JsonKey(
    
    name: r'firstSeq',
    required: false,
    includeIfNull: false,
  )


  final int? firstSeq;





    @override
    bool operator ==(Object other) => identical(this, other) || other is EntityLink &&
      other.sourceId == sourceId &&
      other.externalId == externalId &&
      other.keyType == keyType &&
      other.keyValue == keyValue &&
      other.confidence == confidence &&
      other.validFrom == validFrom &&
      other.validTo == validTo &&
      other.firstSeq == firstSeq;

    @override
    int get hashCode =>
        sourceId.hashCode +
        externalId.hashCode +
        keyType.hashCode +
        keyValue.hashCode +
        confidence.hashCode +
        validFrom.hashCode +
        (validTo == null ? 0 : validTo.hashCode) +
        firstSeq.hashCode;

  factory EntityLink.fromJson(Map<String, dynamic> json) => _$EntityLinkFromJson(json);

  Map<String, dynamic> toJson() => _$EntityLinkToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

