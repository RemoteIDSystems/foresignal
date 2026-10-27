//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'collection_source.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class CollectionSource {
  /// Returns a new [CollectionSource] instance.
  CollectionSource({

    required  this.sourceId,

    required  this.inCollection,

     this.enabledByUsers,
  });

  @JsonKey(
    
    name: r'sourceId',
    required: true,
    includeIfNull: false,
  )


  final String sourceId;



      /// Whether the instance collects this source at all
  @JsonKey(
    
    name: r'inCollection',
    required: true,
    includeIfNull: false,
  )


  final bool inCollection;



      /// How many users enable it in their view
  @JsonKey(
    
    name: r'enabledByUsers',
    required: false,
    includeIfNull: false,
  )


  final int? enabledByUsers;





    @override
    bool operator ==(Object other) => identical(this, other) || other is CollectionSource &&
      other.sourceId == sourceId &&
      other.inCollection == inCollection &&
      other.enabledByUsers == enabledByUsers;

    @override
    int get hashCode =>
        sourceId.hashCode +
        inCollection.hashCode +
        (enabledByUsers == null ? 0 : enabledByUsers.hashCode);

  factory CollectionSource.fromJson(Map<String, dynamic> json) => _$CollectionSourceFromJson(json);

  Map<String, dynamic> toJson() => _$CollectionSourceToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

