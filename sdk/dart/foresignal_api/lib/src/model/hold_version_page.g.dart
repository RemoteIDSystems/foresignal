// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'hold_version_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$HoldVersionPageCWProxy {
  HoldVersionPage items(List<RecordVersion> items);

  HoldVersionPage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `HoldVersionPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// HoldVersionPage(...).copyWith(id: 12, name: "My name")
  /// ```
  HoldVersionPage call({List<RecordVersion> items, String? nextCursor});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfHoldVersionPage.copyWith(...)` or call `instanceOfHoldVersionPage.copyWith.fieldName(value)` for a single field.
class _$HoldVersionPageCWProxyImpl implements _$HoldVersionPageCWProxy {
  const _$HoldVersionPageCWProxyImpl(this._value);

  final HoldVersionPage _value;

  @override
  HoldVersionPage items(List<RecordVersion> items) => call(items: items);

  @override
  HoldVersionPage nextCursor(String? nextCursor) =>
      call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `HoldVersionPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// HoldVersionPage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  HoldVersionPage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return HoldVersionPage(
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

extension $HoldVersionPageCopyWith on HoldVersionPage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfHoldVersionPage.copyWith(...)` or `instanceOfHoldVersionPage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$HoldVersionPageCWProxy get copyWith => _$HoldVersionPageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

HoldVersionPage _$HoldVersionPageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('HoldVersionPage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items']);
      final val = HoldVersionPage(
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

Map<String, dynamic> _$HoldVersionPageToJson(HoldVersionPage instance) =>
    <String, dynamic>{
      'items': instance.items.map((e) => e.toJson()).toList(),
      'nextCursor': ?instance.nextCursor,
    };
