//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/watchlist_item_input.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'watchlist_items_input.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class WatchlistItemsInput {
  /// Returns a new [WatchlistItemsInput] instance.
  WatchlistItemsInput({

    required  this.items,
  });

  @JsonKey(
    
    name: r'items',
    required: true,
    includeIfNull: false,
  )


  final List<WatchlistItemInput> items;





    @override
    bool operator ==(Object other) => identical(this, other) || other is WatchlistItemsInput &&
      other.items == items;

    @override
    int get hashCode =>
        items.hashCode;

  factory WatchlistItemsInput.fromJson(Map<String, dynamic> json) => _$WatchlistItemsInputFromJson(json);

  Map<String, dynamic> toJson() => _$WatchlistItemsInputToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

