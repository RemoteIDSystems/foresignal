//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'report_send_recipients_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ReportSendRecipientsInner {
  /// Returns a new [ReportSendRecipientsInner] instance.
  ReportSendRecipientsInner({

    required  this.grantee,

    required  this.status,

     this.channelIds,
  });

      /// `principal:{id}` or `group:{id}`
  @JsonKey(
    
    name: r'grantee',
    required: true,
    includeIfNull: false,
  )


  final String grantee;



  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final ReportSendRecipientsInnerStatusEnum status;



  @JsonKey(
    
    name: r'channelIds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? channelIds;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ReportSendRecipientsInner &&
      other.grantee == grantee &&
      other.status == status &&
      other.channelIds == channelIds;

    @override
    int get hashCode =>
        grantee.hashCode +
        status.hashCode +
        channelIds.hashCode;

  factory ReportSendRecipientsInner.fromJson(Map<String, dynamic> json) => _$ReportSendRecipientsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$ReportSendRecipientsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ReportSendRecipientsInnerStatusEnum {
@JsonValue(r'notified')
notified(r'notified'),
@JsonValue(r'granted')
granted(r'granted'),
@JsonValue(r'delivered')
delivered(r'delivered'),
@JsonValue(r'failed')
failed(r'failed');

const ReportSendRecipientsInnerStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


