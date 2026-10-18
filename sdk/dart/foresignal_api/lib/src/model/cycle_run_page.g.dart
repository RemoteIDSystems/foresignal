// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'cycle_run_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$CycleRunPageCWProxy {
  CycleRunPage items(List<CycleRun> items);

  CycleRunPage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CycleRunPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CycleRunPage(...).copyWith(id: 12, name: "My name")
  /// ```
  CycleRunPage call({List<CycleRun> items, String? nextCursor});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfCycleRunPage.copyWith(...)` or call `instanceOfCycleRunPage.copyWith.fieldName(value)` for a single field.
class _$CycleRunPageCWProxyImpl implements _$CycleRunPageCWProxy {
  const _$CycleRunPageCWProxyImpl(this._value);

  final CycleRunPage _value;

  @override
  CycleRunPage items(List<CycleRun> items) => call(items: items);

  @override
  CycleRunPage nextCursor(String? nextCursor) => call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CycleRunPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CycleRunPage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  CycleRunPage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return CycleRunPage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<CycleRun>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $CycleRunPageCopyWith on CycleRunPage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfCycleRunPage.copyWith(...)` or `instanceOfCycleRunPage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$CycleRunPageCWProxy get copyWith => _$CycleRunPageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CycleRunPage _$CycleRunPageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('CycleRunPage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items']);
      final val = CycleRunPage(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => CycleRun.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$CycleRunPageToJson(CycleRunPage instance) =>
    <String, dynamic>{
      'items': instance.items.map((e) => e.toJson()).toList(),
      'nextCursor': ?instance.nextCursor,
    };
