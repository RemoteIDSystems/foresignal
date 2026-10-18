// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'situation_member_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SituationMemberPageCWProxy {
  SituationMemberPage items(List<SituationMember> items);

  SituationMemberPage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SituationMemberPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SituationMemberPage(...).copyWith(id: 12, name: "My name")
  /// ```
  SituationMemberPage call({List<SituationMember> items, String? nextCursor});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSituationMemberPage.copyWith(...)` or call `instanceOfSituationMemberPage.copyWith.fieldName(value)` for a single field.
class _$SituationMemberPageCWProxyImpl implements _$SituationMemberPageCWProxy {
  const _$SituationMemberPageCWProxyImpl(this._value);

  final SituationMemberPage _value;

  @override
  SituationMemberPage items(List<SituationMember> items) => call(items: items);

  @override
  SituationMemberPage nextCursor(String? nextCursor) =>
      call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SituationMemberPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SituationMemberPage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SituationMemberPage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return SituationMemberPage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<SituationMember>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $SituationMemberPageCopyWith on SituationMemberPage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSituationMemberPage.copyWith(...)` or `instanceOfSituationMemberPage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SituationMemberPageCWProxy get copyWith =>
      _$SituationMemberPageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SituationMemberPage _$SituationMemberPageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SituationMemberPage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items']);
      final val = SituationMemberPage(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => SituationMember.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$SituationMemberPageToJson(
  SituationMemberPage instance,
) => <String, dynamic>{
  'items': instance.items.map((e) => e.toJson()).toList(),
  'nextCursor': ?instance.nextCursor,
};
