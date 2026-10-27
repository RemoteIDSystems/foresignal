// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'user_source_view_update.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$UserSourceViewUpdateCWProxy {
  UserSourceViewUpdate sources(List<UserSourcePref> sources);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `UserSourceViewUpdate(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// UserSourceViewUpdate(...).copyWith(id: 12, name: "My name")
  /// ```
  UserSourceViewUpdate call({List<UserSourcePref> sources});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfUserSourceViewUpdate.copyWith(...)` or call `instanceOfUserSourceViewUpdate.copyWith.fieldName(value)` for a single field.
class _$UserSourceViewUpdateCWProxyImpl
    implements _$UserSourceViewUpdateCWProxy {
  const _$UserSourceViewUpdateCWProxyImpl(this._value);

  final UserSourceViewUpdate _value;

  @override
  UserSourceViewUpdate sources(List<UserSourcePref> sources) =>
      call(sources: sources);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `UserSourceViewUpdate(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// UserSourceViewUpdate(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  UserSourceViewUpdate call({Object? sources = const $CopyWithPlaceholder()}) {
    return UserSourceViewUpdate(
      sources: sources == const $CopyWithPlaceholder() || sources == null
          ? _value.sources
          // ignore: cast_nullable_to_non_nullable
          : sources as List<UserSourcePref>,
    );
  }
}

extension $UserSourceViewUpdateCopyWith on UserSourceViewUpdate {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfUserSourceViewUpdate.copyWith(...)` or `instanceOfUserSourceViewUpdate.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$UserSourceViewUpdateCWProxy get copyWith =>
      _$UserSourceViewUpdateCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

UserSourceViewUpdate _$UserSourceViewUpdateFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('UserSourceViewUpdate', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['sources']);
  final val = UserSourceViewUpdate(
    sources: $checkedConvert(
      'sources',
      (v) => (v as List<dynamic>)
          .map((e) => UserSourcePref.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$UserSourceViewUpdateToJson(
  UserSourceViewUpdate instance,
) => <String, dynamic>{
  'sources': instance.sources.map((e) => e.toJson()).toList(),
};
