//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/collection_source.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'collection_set_update.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class CollectionSetUpdate {
  /// Returns a new [CollectionSetUpdate] instance.
  CollectionSetUpdate({

    required  this.sources,
  });

  @JsonKey(
    
    name: r'sources',
    required: true,
    includeIfNull: false,
  )


  final List<CollectionSource> sources;





    @override
    bool operator ==(Object other) => identical(this, other) || other is CollectionSetUpdate &&
      other.sources == sources;

    @override
    int get hashCode =>
        sources.hashCode;

  factory CollectionSetUpdate.fromJson(Map<String, dynamic> json) => _$CollectionSetUpdateFromJson(json);

  Map<String, dynamic> toJson() => _$CollectionSetUpdateToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

