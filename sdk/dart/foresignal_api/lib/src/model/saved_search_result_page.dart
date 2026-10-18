//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/saved_search_hit.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'saved_search_result_page.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SavedSearchResultPage {
  /// Returns a new [SavedSearchResultPage] instance.
  SavedSearchResultPage({

    required  this.items,

    required  this.nextCursor,

     this.ranAt,

     this.semantic,
  });

  @JsonKey(
    
    name: r'items',
    required: true,
    includeIfNull: false,
  )


  final List<SavedSearchHit> items;



      /// Opaque cursor for the next page; null at the end
  @JsonKey(
    
    name: r'nextCursor',
    required: true,
    includeIfNull: true,
  )


  final String? nextCursor;



  @JsonKey(
    
    name: r'ranAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? ranAt;



      /// Whether semantic (embedding) search contributed
  @JsonKey(
    
    name: r'semantic',
    required: false,
    includeIfNull: false,
  )


  final bool? semantic;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SavedSearchResultPage &&
      other.items == items &&
      other.nextCursor == nextCursor &&
      other.ranAt == ranAt &&
      other.semantic == semantic;

    @override
    int get hashCode =>
        items.hashCode +
        (nextCursor == null ? 0 : nextCursor.hashCode) +
        ranAt.hashCode +
        semantic.hashCode;

  factory SavedSearchResultPage.fromJson(Map<String, dynamic> json) => _$SavedSearchResultPageFromJson(json);

  Map<String, dynamic> toJson() => _$SavedSearchResultPageToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

