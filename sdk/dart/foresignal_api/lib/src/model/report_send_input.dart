//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'report_send_input.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ReportSendInput {
  /// Returns a new [ReportSendInput] instance.
  ReportSendInput({

     this.principals,

     this.groups,

     this.channels,

     this.message,

     this.revision,
  });

      /// Principal ids or handles
  @JsonKey(
    
    name: r'principals',
    required: false,
    includeIfNull: false,
  )


  final List<String>? principals;



  @JsonKey(
    
    name: r'groups',
    required: false,
    includeIfNull: false,
  )


  final List<String>? groups;



      /// Recipients' own channel ids (130 preferences) for HTML or link delivery
  @JsonKey(
    
    name: r'channels',
    required: false,
    includeIfNull: false,
  )


  final List<String>? channels;



  @JsonKey(
    
    name: r'message',
    required: false,
    includeIfNull: false,
  )


  final String? message;



      /// Default: latest published revision
  @JsonKey(
    
    name: r'revision',
    required: false,
    includeIfNull: false,
  )


  final int? revision;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ReportSendInput &&
      other.principals == principals &&
      other.groups == groups &&
      other.channels == channels &&
      other.message == message &&
      other.revision == revision;

    @override
    int get hashCode =>
        principals.hashCode +
        groups.hashCode +
        channels.hashCode +
        (message == null ? 0 : message.hashCode) +
        (revision == null ? 0 : revision.hashCode);

  factory ReportSendInput.fromJson(Map<String, dynamic> json) => _$ReportSendInputFromJson(json);

  Map<String, dynamic> toJson() => _$ReportSendInputToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

