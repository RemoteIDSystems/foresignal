//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/infra_edge.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'infra_edge_page.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class InfraEdgePage {
  /// Returns a new [InfraEdgePage] instance.
  InfraEdgePage({

    required  this.items,

     this.nextCursor,
  });

  @JsonKey(
    
    name: r'items',
    required: true,
    includeIfNull: false,
  )


  final List<InfraEdge> items;



      /// Opaque cursor for the next page; null on the last page
  @JsonKey(
    
    name: r'nextCursor',
    required: false,
    includeIfNull: false,
  )


  final String? nextCursor;





    @override
    bool operator ==(Object other) => identical(this, other) || other is InfraEdgePage &&
      other.items == items &&
      other.nextCursor == nextCursor;

    @override
    int get hashCode =>
        items.hashCode +
        (nextCursor == null ? 0 : nextCursor.hashCode);

  factory InfraEdgePage.fromJson(Map<String, dynamic> json) => _$InfraEdgePageFromJson(json);

  Map<String, dynamic> toJson() => _$InfraEdgePageToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

