//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/federation_item.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'federation_snapshot_page.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class FederationSnapshotPage {
  /// Returns a new [FederationSnapshotPage] instance.
  FederationSnapshotPage({

    required  this.items,

    required  this.atSeq,

     this.nextCursor,
  });

  @JsonKey(
    
    name: r'items',
    required: true,
    includeIfNull: false,
  )


  final List<FederationItem> items;



      /// Watermark the snapshot is pinned to; resume the change feed from here
  @JsonKey(
    
    name: r'atSeq',
    required: true,
    includeIfNull: false,
  )


  final int atSeq;



  @JsonKey(
    
    name: r'nextCursor',
    required: false,
    includeIfNull: false,
  )


  final String? nextCursor;





    @override
    bool operator ==(Object other) => identical(this, other) || other is FederationSnapshotPage &&
      other.items == items &&
      other.atSeq == atSeq &&
      other.nextCursor == nextCursor;

    @override
    int get hashCode =>
        items.hashCode +
        atSeq.hashCode +
        (nextCursor == null ? 0 : nextCursor.hashCode);

  factory FederationSnapshotPage.fromJson(Map<String, dynamic> json) => _$FederationSnapshotPageFromJson(json);

  Map<String, dynamic> toJson() => _$FederationSnapshotPageToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

