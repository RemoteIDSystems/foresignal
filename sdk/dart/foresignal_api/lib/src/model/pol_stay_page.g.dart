// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'pol_stay_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PolStayPageCWProxy {
  PolStayPage items(List<PolStay> items);

  PolStayPage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PolStayPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PolStayPage(...).copyWith(id: 12, name: "My name")
  /// ```
  PolStayPage call({List<PolStay> items, String? nextCursor});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPolStayPage.copyWith(...)` or call `instanceOfPolStayPage.copyWith.fieldName(value)` for a single field.
class _$PolStayPageCWProxyImpl implements _$PolStayPageCWProxy {
  const _$PolStayPageCWProxyImpl(this._value);

  final PolStayPage _value;

  @override
  PolStayPage items(List<PolStay> items) => call(items: items);

  @override
  PolStayPage nextCursor(String? nextCursor) => call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PolStayPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PolStayPage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PolStayPage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return PolStayPage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<PolStay>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $PolStayPageCopyWith on PolStayPage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPolStayPage.copyWith(...)` or `instanceOfPolStayPage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PolStayPageCWProxy get copyWith => _$PolStayPageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PolStayPage _$PolStayPageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('PolStayPage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items']);
      final val = PolStayPage(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => PolStay.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$PolStayPageToJson(PolStayPage instance) =>
    <String, dynamic>{
      'items': instance.items.map((e) => e.toJson()).toList(),
      'nextCursor': ?instance.nextCursor,
    };
