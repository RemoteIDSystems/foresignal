//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/taxii_collection.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'taxii_collections.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class TaxiiCollections {
  /// Returns a new [TaxiiCollections] instance.
  TaxiiCollections({

     this.collections,
  });

  @JsonKey(
    
    name: r'collections',
    required: false,
    includeIfNull: false,
  )


  final List<TaxiiCollection>? collections;





    @override
    bool operator ==(Object other) => identical(this, other) || other is TaxiiCollections &&
      other.collections == collections;

    @override
    int get hashCode =>
        collections.hashCode;

  factory TaxiiCollections.fromJson(Map<String, dynamic> json) => _$TaxiiCollectionsFromJson(json);

  Map<String, dynamic> toJson() => _$TaxiiCollectionsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

