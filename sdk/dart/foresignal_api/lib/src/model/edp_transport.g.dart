// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'edp_transport.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EdpTransportCWProxy {
  EdpTransport push(bool? push);

  EdpTransport pull(EdpPull? pull);

  EdpTransport search(EdpSearch? search);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EdpTransport(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EdpTransport(...).copyWith(id: 12, name: "My name")
  /// ```
  EdpTransport call({bool? push, EdpPull? pull, EdpSearch? search});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEdpTransport.copyWith(...)` or call `instanceOfEdpTransport.copyWith.fieldName(value)` for a single field.
class _$EdpTransportCWProxyImpl implements _$EdpTransportCWProxy {
  const _$EdpTransportCWProxyImpl(this._value);

  final EdpTransport _value;

  @override
  EdpTransport push(bool? push) => call(push: push);

  @override
  EdpTransport pull(EdpPull? pull) => call(pull: pull);

  @override
  EdpTransport search(EdpSearch? search) => call(search: search);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EdpTransport(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EdpTransport(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  EdpTransport call({
    Object? push = const $CopyWithPlaceholder(),
    Object? pull = const $CopyWithPlaceholder(),
    Object? search = const $CopyWithPlaceholder(),
  }) {
    return EdpTransport(
      push: push == const $CopyWithPlaceholder()
          ? _value.push
          // ignore: cast_nullable_to_non_nullable
          : push as bool?,
      pull: pull == const $CopyWithPlaceholder()
          ? _value.pull
          // ignore: cast_nullable_to_non_nullable
          : pull as EdpPull?,
      search: search == const $CopyWithPlaceholder()
          ? _value.search
          // ignore: cast_nullable_to_non_nullable
          : search as EdpSearch?,
    );
  }
}

extension $EdpTransportCopyWith on EdpTransport {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEdpTransport.copyWith(...)` or `instanceOfEdpTransport.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EdpTransportCWProxy get copyWith => _$EdpTransportCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EdpTransport _$EdpTransportFromJson(Map<String, dynamic> json) =>
    $checkedCreate('EdpTransport', json, ($checkedConvert) {
      final val = EdpTransport(
        push: $checkedConvert('push', (v) => v as bool?),
        pull: $checkedConvert(
          'pull',
          (v) => v == null ? null : EdpPull.fromJson(v as Map<String, dynamic>),
        ),
        search: $checkedConvert(
          'search',
          (v) =>
              v == null ? null : EdpSearch.fromJson(v as Map<String, dynamic>),
        ),
      );
      return val;
    });

Map<String, dynamic> _$EdpTransportToJson(EdpTransport instance) =>
    <String, dynamic>{
      'push': ?instance.push,
      'pull': ?instance.pull?.toJson(),
      'search': ?instance.search?.toJson(),
    };
