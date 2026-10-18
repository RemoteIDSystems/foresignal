// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'report_send_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ReportSendInputCWProxy {
  ReportSendInput principals(List<String>? principals);

  ReportSendInput groups(List<String>? groups);

  ReportSendInput channels(List<String>? channels);

  ReportSendInput message(String? message);

  ReportSendInput revision(int? revision);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ReportSendInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ReportSendInput(...).copyWith(id: 12, name: "My name")
  /// ```
  ReportSendInput call({
    List<String>? principals,
    List<String>? groups,
    List<String>? channels,
    String? message,
    int? revision,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfReportSendInput.copyWith(...)` or call `instanceOfReportSendInput.copyWith.fieldName(value)` for a single field.
class _$ReportSendInputCWProxyImpl implements _$ReportSendInputCWProxy {
  const _$ReportSendInputCWProxyImpl(this._value);

  final ReportSendInput _value;

  @override
  ReportSendInput principals(List<String>? principals) =>
      call(principals: principals);

  @override
  ReportSendInput groups(List<String>? groups) => call(groups: groups);

  @override
  ReportSendInput channels(List<String>? channels) => call(channels: channels);

  @override
  ReportSendInput message(String? message) => call(message: message);

  @override
  ReportSendInput revision(int? revision) => call(revision: revision);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ReportSendInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ReportSendInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ReportSendInput call({
    Object? principals = const $CopyWithPlaceholder(),
    Object? groups = const $CopyWithPlaceholder(),
    Object? channels = const $CopyWithPlaceholder(),
    Object? message = const $CopyWithPlaceholder(),
    Object? revision = const $CopyWithPlaceholder(),
  }) {
    return ReportSendInput(
      principals: principals == const $CopyWithPlaceholder()
          ? _value.principals
          // ignore: cast_nullable_to_non_nullable
          : principals as List<String>?,
      groups: groups == const $CopyWithPlaceholder()
          ? _value.groups
          // ignore: cast_nullable_to_non_nullable
          : groups as List<String>?,
      channels: channels == const $CopyWithPlaceholder()
          ? _value.channels
          // ignore: cast_nullable_to_non_nullable
          : channels as List<String>?,
      message: message == const $CopyWithPlaceholder()
          ? _value.message
          // ignore: cast_nullable_to_non_nullable
          : message as String?,
      revision: revision == const $CopyWithPlaceholder()
          ? _value.revision
          // ignore: cast_nullable_to_non_nullable
          : revision as int?,
    );
  }
}

extension $ReportSendInputCopyWith on ReportSendInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfReportSendInput.copyWith(...)` or `instanceOfReportSendInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ReportSendInputCWProxy get copyWith => _$ReportSendInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ReportSendInput _$ReportSendInputFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ReportSendInput', json, ($checkedConvert) {
      final val = ReportSendInput(
        principals: $checkedConvert(
          'principals',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        groups: $checkedConvert(
          'groups',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        channels: $checkedConvert(
          'channels',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        message: $checkedConvert('message', (v) => v as String?),
        revision: $checkedConvert('revision', (v) => (v as num?)?.toInt()),
      );
      return val;
    });

Map<String, dynamic> _$ReportSendInputToJson(ReportSendInput instance) =>
    <String, dynamic>{
      'principals': ?instance.principals,
      'groups': ?instance.groups,
      'channels': ?instance.channels,
      'message': ?instance.message,
      'revision': ?instance.revision,
    };
