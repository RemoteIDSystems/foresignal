//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'taxii_collection.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class TaxiiCollection {
  /// Returns a new [TaxiiCollection] instance.
  TaxiiCollection({

    required  this.id,

    required  this.title,

     this.description,

    required  this.canRead,

    required  this.canWrite,

     this.mediaTypes,
  });

  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'title',
    required: true,
    includeIfNull: false,
  )


  final String title;



  @JsonKey(
    
    name: r'description',
    required: false,
    includeIfNull: false,
  )


  final String? description;



  @JsonKey(
    
    name: r'can_read',
    required: true,
    includeIfNull: false,
  )


  final bool canRead;



  @JsonKey(
    
    name: r'can_write',
    required: true,
    includeIfNull: false,
  )


  final bool canWrite;



  @JsonKey(
    
    name: r'media_types',
    required: false,
    includeIfNull: false,
  )


  final List<String>? mediaTypes;





    @override
    bool operator ==(Object other) => identical(this, other) || other is TaxiiCollection &&
      other.id == id &&
      other.title == title &&
      other.description == description &&
      other.canRead == canRead &&
      other.canWrite == canWrite &&
      other.mediaTypes == mediaTypes;

    @override
    int get hashCode =>
        id.hashCode +
        title.hashCode +
        (description == null ? 0 : description.hashCode) +
        canRead.hashCode +
        canWrite.hashCode +
        mediaTypes.hashCode;

  factory TaxiiCollection.fromJson(Map<String, dynamic> json) => _$TaxiiCollectionFromJson(json);

  Map<String, dynamic> toJson() => _$TaxiiCollectionToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

