// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'user_source_view.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$UserSourceViewCWProxy {
  UserSourceView sources(List<UserSourcePref> sources);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `UserSourceView(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// UserSourceView(...).copyWith(id: 12, name: "My name")
  /// ```
  UserSourceView call({List<UserSourcePref> sources});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfUserSourceView.copyWith(...)` or call `instanceOfUserSourceView.copyWith.fieldName(value)` for a single field.
class _$UserSourceViewCWProxyImpl implements _$UserSourceViewCWProxy {
  const _$UserSourceViewCWProxyImpl(this._value);

  final UserSourceView _value;

  @override
  UserSourceView sources(List<UserSourcePref> sources) =>
      call(sources: sources);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `UserSourceView(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// UserSourceView(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  UserSourceView call({Object? sources = const $CopyWithPlaceholder()}) {
    return UserSourceView(
      sources: sources == const $CopyWithPlaceholder() || sources == null
          ? _value.sources
          // ignore: cast_nullable_to_non_nullable
          : sources as List<UserSourcePref>,
    );
  }
}

extension $UserSourceViewCopyWith on UserSourceView {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfUserSourceView.copyWith(...)` or `instanceOfUserSourceView.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$UserSourceViewCWProxy get copyWith => _$UserSourceViewCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

UserSourceView _$UserSourceViewFromJson(Map<String, dynamic> json) =>
    $checkedCreate('UserSourceView', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['sources']);
      final val = UserSourceView(
        sources: $checkedConvert(
          'sources',
          (v) => (v as List<dynamic>)
              .map((e) => UserSourcePref.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$UserSourceViewToJson(UserSourceView instance) =>
    <String, dynamic>{
      'sources': instance.sources.map((e) => e.toJson()).toList(),
    };
