from telethon import TelegramClient, events
import config

session_name = 'session'

async def main():
    async with TelegramClient(session_name, config.api_id, config.api_hash) as client:
        @client.on(events.ChatAction)
        async def handler(event):
            # Проверяем, добавлен ли бот в чат
            if event.user_added or event.user_joined:
                chat_id = event.chat_id
                print(f"Бот был добавлен в чат с ID: {chat_id}")

                # Получаем всех участников чата
                async for user in client.iter_participants(chat_id):
                    print(f"Участник чата: ID {user.id} Имя {user.first_name}")

        await client.start(config.phone_number)
        await client.run_until_disconnected()

if __name__ == '__main__':
    import asyncio
    asyncio.run(main())