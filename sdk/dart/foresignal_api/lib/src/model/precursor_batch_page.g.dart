// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'precursor_batch_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PrecursorBatchPageCWProxy {
  PrecursorBatchPage items(List<PrecursorBatch> items);

  PrecursorBatchPage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PrecursorBatchPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PrecursorBatchPage(...).copyWith(id: 12, name: "My name")
  /// ```
  PrecursorBatchPage call({List<PrecursorBatch> items, String? nextCursor});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPrecursorBatchPage.copyWith(...)` or call `instanceOfPrecursorBatchPage.copyWith.fieldName(value)` for a single field.
class _$PrecursorBatchPageCWProxyImpl implements _$PrecursorBatchPageCWProxy {
  const _$PrecursorBatchPageCWProxyImpl(this._value);

  final PrecursorBatchPage _value;

  @override
  PrecursorBatchPage items(List<PrecursorBatch> items) => call(items: items);

  @override
  PrecursorBatchPage nextCursor(String? nextCursor) =>
      call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PrecursorBatchPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PrecursorBatchPage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PrecursorBatchPage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return PrecursorBatchPage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<PrecursorBatch>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $PrecursorBatchPageCopyWith on PrecursorBatchPage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPrecursorBatchPage.copyWith(...)` or `instanceOfPrecursorBatchPage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PrecursorBatchPageCWProxy get copyWith =>
      _$PrecursorBatchPageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PrecursorBatchPage _$PrecursorBatchPageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('PrecursorBatchPage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items']);
      final val = PrecursorBatchPage(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => PrecursorBatch.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$PrecursorBatchPageToJson(PrecursorBatchPage instance) =>
    <String, dynamic>{
      'items': instance.items.map((e) => e.toJson()).toList(),
      'nextCursor': ?instance.nextCursor,
    };
