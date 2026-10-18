//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/federation_item.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'federation_ingest_batch.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class FederationIngestBatch {
  /// Returns a new [FederationIngestBatch] instance.
  FederationIngestBatch({

    required  this.items,
  });

  @JsonKey(
    
    name: r'items',
    required: true,
    includeIfNull: false,
  )


  final List<FederationItem> items;





    @override
    bool operator ==(Object other) => identical(this, other) || other is FederationIngestBatch &&
      other.items == items;

    @override
    int get hashCode =>
        items.hashCode;

  factory FederationIngestBatch.fromJson(Map<String, dynamic> json) => _$FederationIngestBatchFromJson(json);

  Map<String, dynamic> toJson() => _$FederationIngestBatchToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

