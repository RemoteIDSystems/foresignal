//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/federation_item.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'federation_change_page.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class FederationChangePage {
  /// Returns a new [FederationChangePage] instance.
  FederationChangePage({

    required  this.items,

    required  this.lastSeq,

    required  this.hasMore,

     this.watermarkSeq,
  });

  @JsonKey(
    
    name: r'items',
    required: true,
    includeIfNull: false,
  )


  final List<FederationItem> items;



      /// Pass as `after` next time
  @JsonKey(
    
    name: r'lastSeq',
    required: true,
    includeIfNull: false,
  )


  final int lastSeq;



  @JsonKey(
    
    name: r'hasMore',
    required: true,
    includeIfNull: false,
  )


  final bool hasMore;



  @JsonKey(
    
    name: r'watermarkSeq',
    required: false,
    includeIfNull: false,
  )


  final int? watermarkSeq;





    @override
    bool operator ==(Object other) => identical(this, other) || other is FederationChangePage &&
      other.items == items &&
      other.lastSeq == lastSeq &&
      other.hasMore == hasMore &&
      other.watermarkSeq == watermarkSeq;

    @override
    int get hashCode =>
        items.hashCode +
        lastSeq.hashCode +
        hasMore.hashCode +
        watermarkSeq.hashCode;

  factory FederationChangePage.fromJson(Map<String, dynamic> json) => _$FederationChangePageFromJson(json);

  Map<String, dynamic> toJson() => _$FederationChangePageToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

