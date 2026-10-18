//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/fetch_log_entry.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'fetch_page.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class FetchPage {
  /// Returns a new [FetchPage] instance.
  FetchPage({

    required  this.items,

     this.nextAfter,
  });

  @JsonKey(
    
    name: r'items',
    required: true,
    includeIfNull: false,
  )


  final List<FetchLogEntry> items;



      /// Pass as `after` for the next page; null on the last page
  @JsonKey(
    
    name: r'nextAfter',
    required: false,
    includeIfNull: false,
  )


  final int? nextAfter;





    @override
    bool operator ==(Object other) => identical(this, other) || other is FetchPage &&
      other.items == items &&
      other.nextAfter == nextAfter;

    @override
    int get hashCode =>
        items.hashCode +
        (nextAfter == null ? 0 : nextAfter.hashCode);

  factory FetchPage.fromJson(Map<String, dynamic> json) => _$FetchPageFromJson(json);

  Map<String, dynamic> toJson() => _$FetchPageToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

