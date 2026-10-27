//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/collection_source.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'collection_set.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class CollectionSet {
  /// Returns a new [CollectionSet] instance.
  CollectionSet({

    required  this.sources,
  });

  @JsonKey(
    
    name: r'sources',
    required: true,
    includeIfNull: false,
  )


  final List<CollectionSource> sources;





    @override
    bool operator ==(Object other) => identical(this, other) || other is CollectionSet &&
      other.sources == sources;

    @override
    int get hashCode =>
        sources.hashCode;

  factory CollectionSet.fromJson(Map<String, dynamic> json) => _$CollectionSetFromJson(json);

  Map<String, dynamic> toJson() => _$CollectionSetToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

