// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'sink_test_request.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SinkTestRequestCWProxy {
  SinkTestRequest kind(String? kind);

  SinkTestRequest timeoutSeconds(int? timeoutSeconds);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SinkTestRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SinkTestRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  SinkTestRequest call({String? kind, int? timeoutSeconds});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSinkTestRequest.copyWith(...)` or call `instanceOfSinkTestRequest.copyWith.fieldName(value)` for a single field.
class _$SinkTestRequestCWProxyImpl implements _$SinkTestRequestCWProxy {
  const _$SinkTestRequestCWProxyImpl(this._value);

  final SinkTestRequest _value;

  @override
  SinkTestRequest kind(String? kind) => call(kind: kind);

  @override
  SinkTestRequest timeoutSeconds(int? timeoutSeconds) =>
      call(timeoutSeconds: timeoutSeconds);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SinkTestRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SinkTestRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SinkTestRequest call({
    Object? kind = const $CopyWithPlaceholder(),
    Object? timeoutSeconds = const $CopyWithPlaceholder(),
  }) {
    return SinkTestRequest(
      kind: kind == const $CopyWithPlaceholder()
          ? _value.kind
          // ignore: cast_nullable_to_non_nullable
          : kind as String?,
      timeoutSeconds: timeoutSeconds == const $CopyWithPlaceholder()
          ? _value.timeoutSeconds
          // ignore: cast_nullable_to_non_nullable
          : timeoutSeconds as int?,
    );
  }
}

extension $SinkTestRequestCopyWith on SinkTestRequest {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSinkTestRequest.copyWith(...)` or `instanceOfSinkTestRequest.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SinkTestRequestCWProxy get copyWith => _$SinkTestRequestCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SinkTestRequest _$SinkTestRequestFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SinkTestRequest', json, ($checkedConvert) {
      final val = SinkTestRequest(
        kind: $checkedConvert('kind', (v) => v as String?),
        timeoutSeconds: $checkedConvert(
          'timeoutSeconds',
          (v) => (v as num?)?.toInt() ?? 10,
        ),
      );
      return val;
    });

Map<String, dynamic> _$SinkTestRequestToJson(SinkTestRequest instance) =>
    <String, dynamic>{
      'kind': ?instance.kind,
      'timeoutSeconds': ?instance.timeoutSeconds,
    };
