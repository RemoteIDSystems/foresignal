// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'record_version_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$RecordVersionPageCWProxy {
  RecordVersionPage items(List<RecordVersion> items);

  RecordVersionPage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RecordVersionPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RecordVersionPage(...).copyWith(id: 12, name: "My name")
  /// ```
  RecordVersionPage call({List<RecordVersion> items, String? nextCursor});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfRecordVersionPage.copyWith(...)` or call `instanceOfRecordVersionPage.copyWith.fieldName(value)` for a single field.
class _$RecordVersionPageCWProxyImpl implements _$RecordVersionPageCWProxy {
  const _$RecordVersionPageCWProxyImpl(this._value);

  final RecordVersionPage _value;

  @override
  RecordVersionPage items(List<RecordVersion> items) => call(items: items);

  @override
  RecordVersionPage nextCursor(String? nextCursor) =>
      call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RecordVersionPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RecordVersionPage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  RecordVersionPage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return RecordVersionPage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<RecordVersion>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $RecordVersionPageCopyWith on RecordVersionPage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfRecordVersionPage.copyWith(...)` or `instanceOfRecordVersionPage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$RecordVersionPageCWProxy get copyWith =>
      _$RecordVersionPageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RecordVersionPage _$RecordVersionPageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('RecordVersionPage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items']);
      final val = RecordVersionPage(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => RecordVersion.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$RecordVersionPageToJson(RecordVersionPage instance) =>
    <String, dynamic>{
      'items': instance.items.map((e) => e.toJson()).toList(),
      'nextCursor': ?instance.nextCursor,
    };
